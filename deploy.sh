#!/bin/bash
set -e

echo "🚀 Bootpay Android Bio SDK 배포 시작..."
echo "========================================"

# 프로젝트 루트로 이동
cd /Users/taesupyoon/bootpay/client/android/android-bio

# publish.gradle에서 버전 정보 읽기
PUBLISH_VERSION=$(grep "PUBLISH_VERSION = " publish.gradle | sed 's/.*PUBLISH_VERSION = '\''\(.*\)'\''/\1/')
PUBLISH_GROUP_ID=$(grep "PUBLISH_GROUP_ID = " publish.gradle | sed 's/.*PUBLISH_GROUP_ID = '\''\(.*\)'\''/\1/')
PUBLISH_ARTIFACT_ID=$(grep "PUBLISH_ARTIFACT_ID = " publish.gradle | sed 's/.*PUBLISH_ARTIFACT_ID = '\''\(.*\)'\''/\1/')

# 현재 디렉토리 확인
echo "📁 현재 작업 디렉토리: $(pwd)"
echo "📁 local.properties 파일 확인: $(ls -la local.properties 2>/dev/null || echo '파일 없음')"

echo "📋 배포 정보:"
echo "   Group ID: $PUBLISH_GROUP_ID"
echo "   Artifact ID: $PUBLISH_ARTIFACT_ID"
echo "   Version: $PUBLISH_VERSION"
echo "========================================"

echo "📦 Step 1: 기존 빌드 정리..."
rm -rf bio/build/repo
rm -f android-bio-bundle.zip

echo "📦 Step 2: 새로운 publication 생성..."
./gradlew bio:publishReleasePublicationToLocalRepoRepository

echo "📦 Step 3: 번들 생성..."
cd bio/build/repo

# 디버깅을 위한 변수 확인
echo "📁 PUBLISH_GROUP_ID: $PUBLISH_GROUP_ID"
echo "📁 PUBLISH_ARTIFACT_ID: $PUBLISH_ARTIFACT_ID"
echo "📁 PUBLISH_VERSION: $PUBLISH_VERSION"

# 경로 변수 생성
GROUP_PATH=$(echo $PUBLISH_GROUP_ID | sed 's/\./\//g')
echo "📁 GROUP_PATH: $GROUP_PATH"

# zip 명령 실행
zip -r ../../android-bio-bundle.zip \
  ${GROUP_PATH}/${PUBLISH_ARTIFACT_ID}/${PUBLISH_VERSION}/*.aar \
  ${GROUP_PATH}/${PUBLISH_ARTIFACT_ID}/${PUBLISH_VERSION}/*.pom \
  ${GROUP_PATH}/${PUBLISH_ARTIFACT_ID}/${PUBLISH_VERSION}/*.module \
  ${GROUP_PATH}/${PUBLISH_ARTIFACT_ID}/${PUBLISH_VERSION}/*.md5 \
  ${GROUP_PATH}/${PUBLISH_ARTIFACT_ID}/${PUBLISH_VERSION}/*.sha1 \
  ${GROUP_PATH}/${PUBLISH_ARTIFACT_ID}/${PUBLISH_VERSION}/*.asc

cd ../../

echo "✅ 번들 생성 완료: $(ls -lh android-bio-bundle.zip)"

echo "🔐 Step 4: 인증 정보 설정..."
# 현재 디렉토리 재확인
echo "📁 Step 4 현재 디렉토리: $(pwd)"
echo "📁 Step 4 local.properties 파일 확인: $(ls -la local.properties 2>/dev/null || echo '파일 없음')"

# local.properties에서 인증 정보 읽기 (절대 경로 사용)
LOCAL_PROPERTIES_PATH="/Users/taesupyoon/bootpay/client/android/android-bio/local.properties"
if [ -f "$LOCAL_PROPERTIES_PATH" ]; then
    OSSRH_USERNAME=$(grep "^ossrhUsername=" "$LOCAL_PROPERTIES_PATH" | cut -d'=' -f2)
    OSSRH_PASSWORD=$(grep "^ossrhPassword=" "$LOCAL_PROPERTIES_PATH" | cut -d'=' -f2)

    if [ -z "$OSSRH_USERNAME" ] || [ -z "$OSSRH_PASSWORD" ]; then
        echo "❌ local.properties에서 ossrhUsername 또는 ossrhPassword를 찾을 수 없습니다."
        exit 1
    fi

    echo "✅ 인증 정보를 local.properties에서 읽어왔습니다."
else
    echo "❌ local.properties 파일을 찾을 수 없습니다: $LOCAL_PROPERTIES_PATH"
    exit 1
fi
BEARER_TOKEN=$(echo -n "${OSSRH_USERNAME}:${OSSRH_PASSWORD}" | base64)

echo "⬆️  Step 5: Central Portal에 업로드..."
DEPLOYMENT_ID=$(curl --silent --request POST \
  --header "Authorization: Bearer ${BEARER_TOKEN}" \
  --form bundle=@android-bio-bundle.zip \
  https://central.sonatype.com/api/v1/publisher/upload)

if [ -z "$DEPLOYMENT_ID" ]; then
    echo "❌ 업로드 실패!"
    exit 1
fi

echo "✅ 업로드 성공!"
echo "📋 Deployment ID: $DEPLOYMENT_ID"

echo "⏳ Step 6: 배포 상태 확인 중..."
sleep 5

echo "📊 Step 7: 상태 조회..."
STATUS_RESPONSE=$(curl --silent --request POST \
  --header "Authorization: Bearer ${BEARER_TOKEN}" \
  "https://central.sonatype.com/api/v1/publisher/status?id=${DEPLOYMENT_ID}")

echo "📄 배포 상태:"
echo "$STATUS_RESPONSE" | jq .

# 상태 확인
DEPLOYMENT_STATE=$(echo "$STATUS_RESPONSE" | jq -r '.deploymentState')
echo ""
echo "========================================"
echo "🎯 현재 상태: $DEPLOYMENT_STATE"

case $DEPLOYMENT_STATE in
    "PENDING")
        echo "⏳ 검증 대기 중입니다."
        ;;
    "VALIDATING")
        echo "🔍 검증 진행 중입니다."
        ;;
    "VALIDATED")
        echo "✅ 검증 완료! 수동 배포가 필요합니다."
        echo "🚀 자동 배포를 시도합니다..."
        
        PUBLISH_RESPONSE=$(curl --silent --request POST \
          --header "Authorization: Bearer ${BEARER_TOKEN}" \
          --write-out "HTTPSTATUS:%{http_code}" \
          "https://central.sonatype.com/api/v1/publisher/deployment/${DEPLOYMENT_ID}")
        
        HTTP_STATUS=$(echo $PUBLISH_RESPONSE | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
        
        if [ "$HTTP_STATUS" -eq "204" ]; then
            echo "🎉 배포 시작됨! Maven Central에 곧 반영됩니다."
        else
            echo "⚠️  수동 배포 실패. Central Portal에서 수동으로 배포하세요."
        fi
        ;;
    "PUBLISHING")
        echo "🚀 Maven Central에 배포 중입니다."
        ;;
    "PUBLISHED")
        echo "🎉 배포 완료! Maven Central에서 사용 가능합니다."
        ;;
    "FAILED")
        echo "❌ 배포 실패!"
        echo "🔍 오류 내용:"
        echo "$STATUS_RESPONSE" | jq '.errors'
        ;;
    *)
        echo "❓ 알 수 없는 상태: $DEPLOYMENT_STATE"
        ;;
esac

echo ""
echo "========================================"
echo "🌐 Central Portal 확인: https://central.sonatype.com/"
echo "📋 Deployment ID: $DEPLOYMENT_ID"
echo "🏁 스크립트 완료!"
