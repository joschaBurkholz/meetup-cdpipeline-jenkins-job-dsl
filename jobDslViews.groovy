def views = []
views.add([name: 'Docker', regex: 'cdpipeline-docker.*', description: 'Docker Build Jobs'])
views.add([name: 'Admin', regex: '.*seed-job.*', description: 'Job DSL Seed Job'])
views.add([name: 'HelloWorld', regex: '.*meetup.*', description: 'HelloWorld application'])
views.add([name: 'Ansible', regex: 'ansible.*', description: 'Ansible Jobs'])
views.add([name: 'Job DSL', regex: 'job-dsl-.*', description: 'Job DSL'])

views.each {
  def viewName = it.name
  def viewDescription = it.description
  def viewRegularExpression = it.regex
  println "Creating view for ${viewName}"
  listView(viewName) {
    description(viewDescription)
    jobs {
        regex(viewRegularExpression)
    }
    columns {
      buildButton()
      status()
      weather()
      name()
      lastSuccess()
      lastFailure()
      lastDuration()
    }
  }
}

buildMonitorView('Build Monitor') {
    description('All jobs for HelloWorld')
    jobs {
        name('Build Monitor')
        regex('.*meetup.*')
    }
}
