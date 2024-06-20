#!/usr/bin/env bash

# 환경 설정
DOCKER_IMAGE=${1}
DOCKER_TAG=${2}
DOCKER_CONTAINER_NAME="maru-egg-dev"
PROJECT_ROOT="/home/ubuntu/app"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 로그 출력
echo "$TIME_NOW > 배포 스크립트 실행" >> $DEPLOY_LOG

# 기존 컨테이너 중지 및 삭제
echo "$TIME_NOW > 기존 컨테이너 중지 및 삭제" >> $DEPLOY_LOG
sudo docker stop $DOCKER_CONTAINER_NAME || true
sudo docker rm $DOCKER_CONTAINER_NAME || true

# Docker 이미지 Pull
echo "$TIME_NOW > Docker 이미지 풀링: $DOCKER_IMAGE:$DOCKER_TAG" >> $DEPLOY_LOG
sudo docker pull $DOCKER_IMAGE:$DOCKER_TAG

# 새로운 컨테이너 실행
echo "$TIME_NOW > 새로운 컨테이너 실행" >> $DEPLOY_LOG
sudo docker run -d --log-driver=syslog --name $DOCKER_CONTAINER_NAME -p 8080:8080 $DOCKER_IMAGE:$DOCKER_TAG

# 불필요한 이미지 삭제
echo "$TIME_NOW > 불필요한 Docker 이미지 삭제" >> $DEPLOY_LOG
sudo docker image prune -f