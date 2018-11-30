package com.serverless.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitUtils {

    public static Git clone(String repoUrl, File dir, String branch) throws GitAPIException, IOException {
        Files.createDirectories(dir.toPath());

        return Git.cloneRepository()
            .setURI(repoUrl)
            .setDirectory(dir)
            .setBranch(branch)
            .call();
    }

    public static Stream<ObjectId> getRevTrees(Stream<RevCommit> commitsStream) {
        return commitsStream
            .map(rev -> rev.getTree().getId());
    }

    public static List<DiffEntry> diff(Repository repository, ObjectId newCommit, ObjectId oldCommit)
        throws IOException {
        DiffFormatter df = new DiffFormatter(new ByteArrayOutputStream());
        df.setRepository(repository);

        return df.scan(newCommit, oldCommit);
    }

    public static List<DiffEntry> diff(Git git, ObjectId newCommit, ObjectId oldCommit) throws IOException {
        return diff(git.getRepository(), newCommit, oldCommit);
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        clone("https://github.com/eclipse/jgit.git", new File("/Users/marcin/Downloads/tmp"), "master");
    }
}
