import groovy.json.JsonSlurper

class JenkinsJobsApi{

    def getProjects(){
        hudson.FilePath workspace = hudson.model.Executor.currentExecutor().getCurrentWorkspace()
        File f = new File("${workspace}/jenkins-jobs.json")
        new JsonSlurper().parseText(f.getText() )
    }

    def getMavenProjects(){
        getProjects().findAll{it.type.equals("maven")}
    }

    def getDockerProjects(){
        getProjects().findAll{it.type.equals("docker")}
    }
}
