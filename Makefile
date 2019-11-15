
DOCKER_COMPOSE_CMD := docker-compose
ACCESS_TOKEN := f69b726d-d40f-4261-b105-1ec7e6bf04d5
CURL_CMD := curl -v --header 'Authorization: Bearer $(ACCESS_TOKEN)'
DCC_ID_SERVER_URL := http://localhost:8080


docker-status:
	@$(DOCKER_COMPOSE_CMD) ps

docker-server-logs:
	@$(DOCKER_COMPOSE_CMD) logs server

docker-nuke:
	@$(DOCKER_COMPOSE_CMD) down -v

docker-build:
	@$(DOCKER_COMPOSE_CMD) build

docker-start: docker-build
	@$(DOCKER_COMPOSE_CMD) up -d

test-donor:
	@$(CURL_CMD) '$(DCC_ID_SERVER_URL)/donor/id?submittedDonorId=myDonor1&submittedProjectId=myProj1&create=true'

test-specimen:
	@$(CURL_CMD) '$(DCC_ID_SERVER_URL)/specimen/id?submittedSpecimenId=mySpecimen1&submittedProjectId=myProj1&create=true'

test-sample:
	@$(CURL_CMD) '$(DCC_ID_SERVER_URL)/sample/id?submittedSampleId=mySample1&submittedProjectId=myProj1&create=true'

test-object:
	@$(CURL_CMD) '$(DCC_ID_SERVER_URL)/object/id?analysisId=myAnalysisId1&fileName=myfile.vcf.gz'

test-analysis-unique:
	@$(CURL_CMD) '$(DCC_ID_SERVER_URL)/analysis/unique'

test-analysis-get:
	@$(CURL_CMD) '$(DCC_ID_SERVER_URL)/analysis/id?submittedAnalysisId=myAnalysisId&create=true'

