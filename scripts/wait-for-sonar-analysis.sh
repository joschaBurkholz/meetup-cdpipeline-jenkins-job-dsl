#!/bin/bash
url=$(cat $WORKSPACE/target/sonar/report-task.txt |grep ceTaskUrl |cut -f2,3 -d '=')
serverUrl=$(cat $WORKSPACE/target/sonar/report-task.txt |grep serverUrl |cut -f2,3 -d '=')
echo "Url: ${url}"
echo "ServerUrl: ${serverUrl}"
status="UNKNOWN"
count=1

until [[ "$status" == "SUCCESS" || "$status" == "CANCELED" || "$status" == "FAILED" || $count -gt 30 ]]
do
	echo "Status ${status} - Abfrage Sonar-Task Nr ${count} - curl ${url}"
	result=$(curl -u admin:admin $url)
	status=$(echo "$result" |jq .task.status|sed -e 's/^"//' -e 's/"$//')
	sleep 1s
	count=$((count+1))
done

if [[ "$status" == "SUCCESS" ]]
then
	analysisID=$(echo $result |jq .task.analysisId|sed -e 's/^"//' -e 's/"$//')
else
	echo "Sonar run was not successful - ${status}"
	exit 1;
fi

analysisUrl="${serverUrl}/api/qualitygates/project_status?analysisId=${analysisID}"
echo $analysisUrl

quality_gate_status=$(curl -u admin:admin123 $analysisUrl |jq .projectStatus.status|sed -e 's/^"//' -e 's/"$//')
echo "Quality Gate Status ${quality_gate_status}"
if [[ "$quality_gate_status" != "OK" ]]; then
	echo "Sonar - Error at quality gate validation"
	exit 0;
fi