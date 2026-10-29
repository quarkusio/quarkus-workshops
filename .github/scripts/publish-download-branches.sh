#!/usr/bin/env bash
# Publishes each workshop download, as assembled by `mvn package -N`, as a branch holding a single
# commit, so that GitHub can serve it as a zip from github.com/<repo>/archive/refs/heads/<branch>.zip.
#
# Each run replaces the branch rather than adding to it, so old copies of the code do not pile up in
# the repository. A branch whose contents have not changed is left alone.
#
# Usage: publish-download-branches.sh <assembly-output-dir> [remote]
set -euo pipefail

assembled=$1
remote=${2:-origin}

declare -A branches=(
    [quarkus-super-heroes-workshop]=download/start
    [quarkus-super-heroes-workshop-core]=download/core
    [quarkus-super-heroes-workshop-complete]=download/complete
)

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

git fetch --quiet --no-tags --prune "$remote" "+refs/heads/download/*:refs/remotes/$remote/download/*"
source_commit=$(git rev-parse HEAD)

refspecs=()
for download in "${!branches[@]}"; do
    branch=${branches[$download]}
    if [[ ! -d $assembled/$download ]]; then
        echo "No $download in $assembled; run mvn package -N first" >&2
        exit 1
    fi

    # Build the tree with a throwaway index, so the checkout's own index and working tree are not
    # touched. -f, because the assembly is the authority on what belongs in it, not any .gitignore.
    export GIT_INDEX_FILE="$work/index-${branch//\//-}"
    git --work-tree="$assembled/$download" add --all --force .
    tree=$(git write-tree)
    unset GIT_INDEX_FILE

    if [[ $(git rev-parse --quiet --verify "refs/remotes/$remote/$branch^{tree}" || true) == "$tree" ]]; then
        echo "$branch is up to date"
        continue
    fi
    commit=$(git commit-tree "$tree" -m "Workshop code from $source_commit")
    refspecs+=("+$commit:refs/heads/$branch")
    echo "$branch will be updated"
done

if [[ ${#refspecs[@]} -gt 0 ]]; then
    # Atomic, so the branches never end up holding code from different commits.
    git push --atomic "$remote" "${refspecs[@]}"
fi
