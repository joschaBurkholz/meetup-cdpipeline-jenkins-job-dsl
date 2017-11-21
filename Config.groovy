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
                nexusUrl               : "http://nexus.joscha-burkholz.de",
                targetReleaseRepoUrl   : "http://nexus.joscha-burkholz.de/content/repositories/releases",
                targetThirdpartyRepoUrl: "http://nexus.joscha-burkholz.de/content/repositories/thirdparty",
                targetSnapshotRepoUrl  : "http://nexus.joscha-burkholz.de/content/repositories/snapshots",
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
