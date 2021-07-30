#!/bin/bash
echo -e "Host $VPS_HOST\n\tStrictHostKeyChecking no\n" >>~/.ssh/config
echo $VPS_KEY | base64 --decode >~/.ssh/travis_rsa
chmod 600 ~/.ssh/travis_rsa
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/travis_rsa
ssh -i ~/.ssh/travis_rsa -o UserKnownHostsFile=/dev/null $VPS_USERNAME@$VPS_HOST "docker version && docker-compose version"
scp docker-compose.yml $VPS_USERNAME@$VPS_HOST:/root/docker-compose
ssh -i ~/.ssh/travis_rsa $VPS_USERNAME@$VPS_HOST "cd /root/docker-compose && export TAG_NAME=${$TRAVIS_BRANCH} && export BOT_TOKEN=${BOT_TOKEN} && export BOT_USERNAME=${BOT_USERNAME} && export PAYMENTS_STRIPE_KEY_PUBLIC=${PAYMENTS_STRIPE_KEY_PUBLIC} && export PAYMENTS_STRIPE_KEY_SECRET=${PAYMENTS_STRIPE_KEY_SECRET} && docker-compose up --detach --force-recreate"
