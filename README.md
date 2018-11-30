This is a demo serverless application for handling github webhooks.

## About

In this application we're using java [JGit](https://www.eclipse.org/jgit/) library to detect hot-spots in changed files.

Hot-spots are the files which are the most frequently edited in the project history.

Modifying such files may introduce a potential bug. It also signals that the file may violate good design practices like
single responsibility principle, especially if it's a large file.

Analysing a history of changes in a project may lead to many more interesting discoveries than just static source code analysis.
The subject is called "code forensics" and more about it can be found in the great book [Your Code as a Crime Scene](https://pragprog.com/book/atcrime/your-code-as-a-crime-scene) by Adam Tornhill. 


## How it works

The service is using [Serverless Framework](https://serverless.com).

Github webhook is configured to invoke HTTP request to AWS API Gateway endpoint.

API Gateway invokes a lambda function (implementation in [`com.serverless.ApiGatewayHandler`](src/main/java/com/serverless/ApiGatewayHandler.java)).

All the analysis could be done in single Lambda function, but because this may take much longer than maximum timeout for API Gateway of 30 sec., 
an another Lambda function  (implementation in [`com.serverless.Job`](src/main/java/com/serverless/Job.java)) is invoked asynchronously and the first function responds immediately to API Gateway.

The second function is limited to max. Lambda execution time which is currently 15 min.

It does the analysis and posts results in pull request comments.

## Why JGit

What is important when working with git on Lambda is that there is no git executable installed. 

So either we have to install it programmatically or we can use any git Java API which does not require git installation.
One of such is [jgit](https://www.eclipse.org/jgit/) library.

You can check my [other post](https://dev.to/piczmar_0/learn-about-your-project-from-git-history-583n) where I explained how we can use it to analyse git history.
I reused the ideas from that post here to fetch the list of hot files.

(Side note: If you use node.js, there is a module which you can use to install git executable: [https://github.com/pimterry/lambda-git](https://github.com/pimterry/lambda-git))


## Feedback to git users

There are a few alternatives of giving feedback after the git history analysis:

1. [Reviews API](https://developer.github.com/v3/pulls/reviews/) 

The API is used to publish comments on each PR update (creation or subsequent commits to the PR), e.g.:

```
curl -X POST -H 'Authorization: token PERSONAL_API_TOKEN' \
  -d '{"event" : "COMMENT", "body" : "Be careful with\n ```notes.txt``` file"}' \
  https://api.github.com/repos/piczmar/git-code-stats/pulls/3/reviews
```


where `PERSONAL_API_TOKEN` is a token generated for Github user, see more about [Personal API Tokens](https://blog.github.com/2013-05-16-personal-api-tokens/).

The advantage of this solution is that individual users can integrate their repository on their own without need from organization
admin to install a Github App.

The drawback is that there is no indicator that the analysis is running on PR page in Github, in contrary when using
 the Checks API mentioned below.

2. [Checks API](https://developer.github.com/v3/checks/runs/#create-a-check-run)

An alternative would be to register a new Github App and use Checks API, which is available only to Github Apps currently.

We could not use it with Personal API Tokens authorization method.

In this service the first approach was taken - the Reviews API.

