package com.issue.importer.cdk;

import com.issue.importer.cdk.construct.DockerRepository;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import static com.issue.importer.cdk.Validations.requireNonEmpty;

public class DockerRepositoryApp {

    public static void main(final String[] args) {
        App app = new App();

        String accountId = requireNonEmpty(app, "accountId");
        String region = requireNonEmpty(app, "region");
        String applicationName = requireNonEmpty(app, "applicationName");

        Environment awsEnvironment = makeEnv(accountId, region);

        Stack dockerRepositoryStack = new Stack(app, "DockerRepositoryStack", StackProps.builder()
                .stackName(applicationName + "-DockerRepository")
                .env(awsEnvironment)
                .build());

        new DockerRepository(
                dockerRepositoryStack,
                "DockerRepository",
                new DockerRepository.DockerRepositoryInputParameters(applicationName, accountId));

        app.synth();
    }

    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
