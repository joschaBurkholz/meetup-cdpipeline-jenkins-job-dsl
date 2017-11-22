class Config {
    static getConfig(dockerRegistryURL) {
        println "DOCKER_REGISTRY: ${dockerRegistryURL}"
        return [
                environment            : 'dev',
                enableBuildTrigger     : false,
                promoteAfterBuild      : 'always',
                gitBaseUrl             : "git@github.com:joschaBurkholz",
                gitCredentials         : "github_ssh_keys",
                httpProxyBuildArg      : "",
                nexusUrl               : "http://joscha-burkholz.de:8081",
                targetReleaseRepoUrl   : "http://joscha-burkholz.de:8081/content/repositories/releases",
                targetThirdpartyRepoUrl: "http://joscha-burkholz.de:8081/content/repositories/thirdparty",
                targetSnapshotRepoUrl  : "http://joscha-burkholz.de:8081/content/repositories/snapshots",
                "environments"         : [
                        [
                                stage                 : 'dev',
                                promoteAfterBuild     : 'always',
                                star                  : "orange"
                        ],
                        [
                                stage                 : 'qa',
                                promoteAfterBuild     : 'manual',
                                star                  : "blue"
                        ]
                ]
          ]
    }
}
