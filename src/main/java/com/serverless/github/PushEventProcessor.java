package com.serverless.github;

import static com.serverless.git.GitDiffCollector.getHotFiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.serverless.git.GitUtils;

/**
 * For github push event it does:
 * <ul>
 * <li>gets a diff on branch</li>
 * <li>gets the list of the most frequently changed files (hot files) in whole commit history</li>
 * <li>compares the list of the hot files with the files modified in diff</li>
 * <li>if any changed files match the hot files it adds a review message to all push requests for the branch</li>
 * </ul>
 */
public class PushEventProcessor {

    private static final Logger LOG = Logger.getLogger(PushEventProcessor.class);

    private static final List<ChangeType> FILTERED_CHANGE_TYPES = Arrays
        .asList(ChangeType.DELETE, ChangeType.MODIFY, ChangeType.RENAME, ChangeType.COPY);
    private static final String LINE_SEPARATOR = "\n";

    private final String personalAccessToken;

    public PushEventProcessor(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public void process(PushEvent event) throws IOException, GitAPIException, UnirestException {

        String branch = event.getRef();
        LOG.info("branch = " + branch);

        PushEvent.Repository repositoryData = event.getRepository();
        String githubUser = repositoryData.getOwner().getName();
        LOG.info("github user = " + githubUser);

        String repositoryName = repositoryData.getName();
        LOG.info("repo name = " + repositoryName);

        String repositoryUrl = repositoryData.getUrl();
        LOG.info("repo url = " + repositoryUrl);

        String checkoutFolder = String.format("/tmp/%s/%s", repositoryName, branch);
        File repoDir = new File(checkoutFolder);
        if (repoDir.exists() && repoDir.isDirectory()) {
            FileUtils.delete(repoDir, FileUtils.RECURSIVE);
        }
        Git git = GitUtils.clone(repositoryUrl, repoDir, branch);

        LOG.info("Running analysis...");

        Repository repository = git.getRepository();
        LOG.info(
            "Branch: " + repository.getBranch() + ", description: " + repository.getRepositoryState()
                .getDescription());

        Map<String, Long> changedHotFiles = getChangedHotFiles(event.getCompare(), git, repository);

        if (!changedHotFiles.isEmpty()) {

            List<PullRequestDto> pullRequests = GithubUtils
                .getPullRequests(githubUser, repositoryName, personalAccessToken, githubUser + ":" + branch);

            String reviewComment = getReviewComment(changedHotFiles);

            publishReview(githubUser, repositoryName, pullRequests, reviewComment);
        }
    }

    private Map<String, Long> getChangedHotFiles(String compareLink, Git git,
                                                 Repository repository) throws IOException, GitAPIException {

        String[] diffHashes = getDiffCommitHashes(compareLink);
        String newCommitHash = diffHashes[1];
        String oldCommitHash = diffHashes[0];

        List<DiffEntry> diff = GitUtils
            .diff(repository, repository.resolve(newCommitHash), repository.resolve(oldCommitHash));

        LOG.info("diff: " + diff);

        List<String> filteredChanges = diff.stream()
            .filter(e -> FILTERED_CHANGE_TYPES.contains(e.getChangeType()))
            .map(DiffEntry::getOldPath)
            .collect(Collectors.toList());

        Map<String, Long> hotSpots = getHotFiles(git, 10);
        LOG.info("Hot spots: ");
        hotSpots.forEach((key, value) -> LOG.info(key + " - " + value));

        return hotSpots.entrySet()
            .stream()
            .filter(e -> filteredChanges.contains(e.getKey()))
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    private void publishReview(String githubUser, String repositoryName, List<PullRequestDto> pullRequests,
                               String reviewComment) {
        pullRequests.forEach(r -> {
            try {
                GithubUtils
                    .publishReview(githubUser, repositoryName, r.getNumber(),
                        personalAccessToken,
                        reviewComment);
            } catch (UnirestException e) {
                LOG.error(String.format("Could not publish review for PR %s", r.getNumber()), e);
            }
        });
    }

    private String getReviewComment(Map<String, Long> changedHotFiles) {
        StringBuilder reviewContent = new StringBuilder(
            ":warning: The most frequently changed files in the history were modified.")
            .append(LINE_SEPARATOR)
            .append("Here they are: ")
            .append(LINE_SEPARATOR);
        String reviewLines = changedHotFiles.entrySet()
            .stream()
            .map(e -> " * " + e.getKey() + " modified " + e.getValue() + " times in the past")
            .collect(Collectors.joining(LINE_SEPARATOR));
        reviewContent.append(reviewLines)
            .append(LINE_SEPARATOR)
            .append(LINE_SEPARATOR)
            .append(
                "Any frequently changed file in the history can be a potential source of issues and needs careful review.");
        return reviewContent.toString();
    }

    private static String[] getDiffCommitHashes(String compareLink) {
        String token = compareLink.substring(compareLink.lastIndexOf('/') + 1);
        return token.split("\\...");
    }

}
