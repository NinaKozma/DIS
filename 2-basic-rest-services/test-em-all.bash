#!/usr/bin/env bash
#
# ./grdelw clean build
# docker-compose build
# docker-compose up -d
#
# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8443}
: ${POST_ID_REA_COMM_IMG=100}
: ${POST_ID_NOT_FOUND=14}
: ${POST_ID_NO_REA=114}
: ${POST_ID_NO_COMM=214}
: ${POST_ID_NO_IMG=314}


function assertCurl() {

    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval $curlCmd)
    local httpCode="${result:(-3)}"
    RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

    if [ "$httpCode" = "$expectedHttpCode" ]
    then
        if [ "$httpCode" = "200" ]
        then
            echo "Test OK (HTTP Code: $httpCode)"
        else
            echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
        fi
        return 0
    else
        echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
        echo  "- Failing command: $curlCmd"
        echo  "- Response Body: $RESPONSE"
        return 1
    fi
}

function assertEqual() {

    local expected=$1
    local actual=$2

    if [ "$actual" = "$expected" ]
    then
        echo "Test OK (actual value: $actual)"
        return 0
    else
        echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
        return 1
    fi
}

function testUrl() {
    url=$@
    if $url -ks -f -o /dev/null
    then
          return 0
    else
          return 1
    fi;
}

function waitForService() {
    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
    echo "DONE, continues..."
}

function testCompositeCreated() {

    # Expect that the Post Composite for postId $POST_ID_REA_COMM_IMG has been created with three reactions and three comments
    if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/post-composite/$POST_ID_REA_COMM_IMG -s"
    then
        echo -n "FAIL"
        return 1
    fi

    set +e
    assertEqual "$POST_ID_REA_COMM_IMG" $(echo $RESPONSE | jq .postId)
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".reactions | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
    if [ "$?" -eq "1" ] ; then return 1; fi
    
    assertEqual 3 $(echo $RESPONSE | jq ".images | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    set -e
}

function waitForMessageProcessing() {
    echo "Wait for messages to be processed... "

    # Give background processing some time to complete...
    sleep 1

    n=0
    until testCompositeCreated
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
    echo "All messages are now processed!"
}

function recreateComposite() {
    local postId=$1
    local composite=$2

    assertCurl 200 "curl $AUTH -X DELETE -k https://$HOST:$PORT/post-composite/${postId} -s"
    curl -X POST -k https://$HOST:$PORT/post-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite"
}

function setupTestdata() {

    body="{\"postId\":$POST_ID_NO_REA"
    body+=\
',"typeOfPost":"instagram post","postCaption":"post caption", "postedOn":"2023-07-15", "comments":[
        {"commentId":1,"commentText":"comment text 1","commentDate":"2023-07-15"},
        {"commentId":2,"commentText":"comment text 2","commentDate":"2023-07-15"},
        {"commentId":3,"commentText":"comment text 3","commentDate":"2023-07-15"}
    ], "images":[
        {"imageId":1,"imageUrl":"image url 1","postedOn":"2023-07-15"},
        {"imageId":2,"imageUrl":"image url 2","postedOn":"2023-07-15"},
        {"imageId":3,"imageUrl":"image url 3","postedOn":"2023-07-15"}
    ]}'
    recreateComposite "$POST_ID_NO_REA" "$body"

    body="{\"postId\":$POST_ID_NO_COMM"
    body+=\
',"typeOfPost":"instagram post","postCaption":"post caption", "postedOn":"2023-07-15", "reactions":[
        {"reactionId":1,"typeOfReaction":"heart"},
        {"reactionId":2,"typeOfReaction":"sad"},
        {"reactionId":3,"typeOfReaction":"laugh"}
    ],"images":[
        {"imageId":1,"imageUrl":"image url 1","postedOn":"2023-07-15"},
        {"imageId":2,"imageUrl":"image url 2","postedOn":"2023-07-15"},
        {"imageId":3,"imageUrl":"image url 3","postedOn":"2023-07-15"}
    ]}'
    recreateComposite "$POST_ID_NO_COMM" "$body"
    
    body="{\"postId\":$POST_ID_NO_IMG"
    body+=\
',"typeOfPost":"instagram post","postCaption":"post caption", "postedOn":"2023-07-15", "comments":[
        {"commentId":1,"commentText":"comment text 1","commentDate":"2023-07-15"},
        {"commentId":2,"commentText":"comment text 2","commentDate":"2023-07-15"},
        {"commentId":3,"commentText":"comment text 3","commentDate":"2023-07-15"}
    ], "reactions":[
        {"reactionId":1,"typeOfReaction":"heart"},
        {"reactionId":2,"typeOfReaction":"sad"},
        {"reactionId":3,"typeOfReaction":"laugh"}
    ]}'
    recreateComposite "$POST_ID_NO_IMG" "$body"


    body="{\"postId\":$POST_ID_REA_COMM_IMG"
    body+=\
',"typeOfPost":"instagram post","postCaption":"post caption", "postedOn":"2023-07-15", "reactions":[
        {"reactionId":1,"typeOfReaction":"heart"},
        {"reactionId":2,"typeOfReaction":"sad"},
        {"reactionId":3,"typeOfReaction":"laugh"}
    ], "comments":[
        {"commentId":1,"commentText":"comment text 1","commentDate":"2023-07-15"},
        {"commentId":2,"commentText":"comment text 2","commentDate":"2023-07-15"},
        {"commentId":3,"commentText":"comment text 3","commentDate":"2023-07-15"}
    ], "images":[
        {"imageId":1,"imageUrl":"image url 1","postedOn":"2023-07-15"},
        {"imageId":2,"imageUrl":"image url 2","postedOn":"2023-07-15"},
        {"imageId":3,"imageUrl":"image url 3","postedOn":"2023-07-15"}
    ]}'
    recreateComposite "$POST_ID_REA_COMM_IMG" "$body"

}

set -e

echo "Start Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
    echo "Restarting the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupTestdata

waitForMessageProcessing


# Verify that a normal request works, expect three reactions, three comments and three images
assertCurl 200 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_REA_COMM_IMG $AUTH -s"
assertEqual "$POST_ID_REA_COMM_IMG" $(echo $RESPONSE | jq .postId)
assertEqual 3 $(echo $RESPONSE | jq ".reactions | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".images | length")

# Verify that a 404 (Not Found) error is returned for a non existing postId ($POST_ID_NOT_FOUND)
assertCurl 404 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_NOT_FOUND $AUTH -s"

# Verify that no reactions are returned for postId $POST_ID_NO_REA
assertCurl 200 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_NO_REA $AUTH -s"
assertEqual "$POST_ID_NO_REA" $(echo $RESPONSE | jq .postId)
assertEqual 0 $(echo $RESPONSE | jq ".reactions | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".images | length")

# Verify that no comments are returned for postId $POST_ID_NO_COMM
assertCurl 200 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_NO_COMM $AUTH -s"
assertEqual $POST_ID_NO_COMM $(echo $RESPONSE | jq .postId)
assertEqual 3 $(echo $RESPONSE | jq ".reactions | length")
assertEqual 0 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".images | length")

# Verify that no images are returned for postId $POST_ID_NO_IMG
assertCurl 200 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_NO_IMG $AUTH -s"
assertEqual $POST_ID_NO_IMG $(echo $RESPONSE | jq .postId)
assertEqual 3 $(echo $RESPONSE | jq ".reactions | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 0 $(echo $RESPONSE | jq ".images | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a postId that is out of range (-1)
assertCurl 422 "curl -k https://$HOST:$PORT/post-composite/-1 $AUTH -s"
assertEqual "\"Invalid postId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a postId that is not a number, i.e. invalid format
assertCurl 400 "curl -k https://$HOST:$PORT/post-composite/invalidPostId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_REA_COMM_IMG -s"
 
# Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""
 
assertCurl 200 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_REA_COMM_IMG $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/post-composite/$POST_ID_REA_COMM_IMG $READER_AUTH -X DELETE -s"

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi
