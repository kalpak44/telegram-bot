#!/bin/bash
echo -e "Host $VPS_HOST\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config
echo $VPS_KEY | base64 --decode > ~/.ssh/travis_rsa
chmod 600 ~/.ssh/travis_rsa
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/travis_rsa
ssh -i ~/.ssh/travis_rsa -o UserKnownHostsFile=/dev/null  $VPS_USERNAME@$VPS_HOST "docker version"