#!/bin/sh

# abort the script if there is a non-zero error
set -e

TARGET_BRANCH="gh-pages"

git clone $CIRCLE_REPOSITORY_URL --branch $TARGET_BRANCH --single-branch out

./compile.sh

cd out

git config user.name "CircleCI"
git config user.email "$GH_EMAIL"
if [ $(git status --porcelain | wc -l) -lt 1 ]; then
    echo "No changes to the output on this push; exiting."
    exit 0
fi

# stage any changes and new files
git add -A
# now commit, ignoring branch gh-pages doesn't seem to work, so trying skip

git commit -m "Deploy to GitHub pages: ${CIRCLE_SHA1}"
# and push, but send any output to /dev/null to hide anything sensitive
git push --force --quiet origin gh-pages
# go back to where we started and remove the gh-pages git repo we made and used
# for deployment
cd ..
rm -rf out

echo "Finished Deployment!"