def config = Config.getConfig(DOCKER_REGISTRY)
def jenkinsJobsApi = new JenkinsJobsApi()
def projects = jenkinsJobsApi.getMavenProjects()
def environments = config.environments

println "############################################################################################################"
println "Iterating all projects"
println ""
projects.each { project ->

    def gitUrl = "${project.ssh_url_to_repo}"
    def gitCredentials = config.gitCredentials
    def jobName = "${project.name}"

    println "############################################################################################################"
    println "Creating Maven Build Job ${jobName}"

    mavenJob(jobName) {
        logRotator {
            numToKeep(10)
        }
        scm {
            git {
                remote {
                    url("${gitUrl}")
                    credentials("${gitCredentials}")
                }
                branch("master")
                extensions {
                    cleanBeforeCheckout()
                    wipeOutWorkspace()
                    gitTagMessageExtension()
                }
            }
        }
        if (config.enableBuildTrigger) {
            triggers {
                scm("H/5 * * * *")
            }
        }
        properties {
            if (!project.library) {
                promotions {
                    environments.each { environment ->
                        promotion {
                            icon("star-${environment.star}")
                            name("${environment.stage}")
                            conditions {
                                if (environment.promoteAfterBuild == 'manual') {
                                    manual("JENKINS_ADMIN")
                                } else {
                                    selfPromotion(true)
                                    if (environment.promoteAfterBuild == 'releases-only') {
                                        releaseBuild()
                                    }
                                }
                            }
                            actions {
                                downstreamParameterized {
                                    trigger(project.promotionJob ?: 'meetup-deployment-for-promote') {
                                        block {
                                            buildStepFailure('FAILURE')
                                            failure('FAILURE')
                                            unstable('UNSTABLE')
                                        }
                                        parameters {
                                            predefinedProp("INVENTORY", "meetup-hello-world-infrastructure")
                                            predefinedProp("ENVIRONMENT", "${environment.stage}")
                                            predefinedProp("JOB_NAME", "\${PROMOTED_JOB_FULL_NAME}")
                                            predefinedProp("BUILD_ID", "\${PROMOTED_NUMBER}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        goals("clean deploy -U -Ddocker.registry=\${DOCKER_REGISTRY}")
        archivingDisabled(true)
        mavenOpts("-Dmaven.test.failure.ignore=false -DaltSnapshotDeploymentRepository=nexus-snapshots-repository::default::${config.targetSnapshotRepoUrl} -DaltReleaseDeploymentRepository=nexus-releases-repository::default::${config.targetReleaseRepoUrl}")
        mavenInstallation('maven')
        //Create build infos
        postBuildSteps {
            shell("echo \"APPLICATION=\${POM_ARTIFACTID}\" >> build.properties\necho \"VERSION=\${POM_VERSION}\" >> build.properties")
            if (!project.disable_sonar) {
                maven {
                    goals('sonar:sonar -U -Psonar')
                    mavenInstallation('maven')
                    mavenOpts('-Xms512m')
                    mavenOpts('-Xmx1536m')
                }
                shell(readFileFromWorkspace('scripts/wait-for-sonar-analysis.sh'))
            }
        }
        publishers {
            archiveArtifacts("build.properties")
            groovyPostBuild {
                script('''manager.addShortText(manager.build.getEnvironment(manager.listener)[\'POM_VERSION\'])
	        	if(manager.logContains(".*Sonar - Error at quality gate validation*")) {
				    manager.addWarningBadge("Sonar Quality Gate")
				    manager.createSummary("warning.gif").appendText("<h1>Sonar Quality Gate Validation!</h1>", false)
				    manager.buildUnstable()
				}''')
                sandbox(false)
            }
            if (project.cloverReportDir) {
                configure { node ->
                    node / 'publishers' << 'hudson.plugins.clover.CloverPublisher'(plugin: "clover@4.7.1") {
                        cloverReportDir(project.cloverReportDir)
                        cloverReportFileName('clover.xml')
                        healthyTarget {
                            methodCoverage(70)
                            conditionalCoverage(70)
                            statementCoverage(80)
                        }
                    }
                }
                publishHtml {
                    report("${project.cloverReportDir}/lcov-report/") {
                        reportName("Coverage report ${project.cloverReportDir.split('/')[0]}")
                        reportFiles('index.html')
                    }
                }
            }
        }
        wrappers {
            release {
                releaseVersionTemplate('Release: ${RELEASE_VERSION}')
                parameters {
                    stringParam('RELEASE_VERSION', '', 'Versionsnummer für das zu erstellende Release (z.B. 1.0.0).')
                }
                preBuildSteps {
                    maven {
                        goals("versions:set")
                        mavenInstallation('maven')
                        property("newVersion", "\${RELEASE_VERSION}")
                        shell("git tag \${RELEASE_VERSION}")
                        shell("git push origin \${RELEASE_VERSION}")
                    }
                }
            }
        }
    }
}
