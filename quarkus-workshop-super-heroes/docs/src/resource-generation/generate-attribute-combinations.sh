#!/bin/bash
set -e

# Define the variable variations
# To disable a variant, just set it to false, rather than removing it (or remove it everywhere, including the pom and the HTML)
ai=("true" "false")
azure=("true" "false")
container=("true" "false")
contract_testing=("true" "false")
extension=("true" "false")
kubernetes=("true" "false")
messaging=("true" "false")
native=("true" "false")
observability=("false")

basedir=$1

# allow the os to be passed in as a parameter, just as a convenience to allow execution to be split up
# if it's not passed in, iterate over all four options
if [ -z ${2+x} ]; then oses=("all" "mac" "linux" "windows"); else oses=($2); fi

# Iterate over the combinations
for os in "${oses[@]}"; do
    for use_ai in "${ai[@]}"; do
        for use_azure in "${azure[@]}"; do
            for use_container in "${container[@]}"; do
                for use_contract_testing in "${contract_testing[@]}"; do
                    for use_extension in "${extension[@]}"; do
                        for use_kubernetes in "${kubernetes[@]}"; do
                            for use_messaging in "${messaging[@]}"; do
                                for use_native in "${native[@]}"; do
                                    for use_observability in "${observability[@]}"; do

                                        # Create a filename based on the combination
                                        # Any changes here should be reflected in the index.html UI, too
                                        # It would be nice to make nested folders but the maven iterator plugin needs a flat set of folders
                                        dirname=${basedir}/os-${os}-native-${use_native}-ai-${use_ai}-kubernetes-${use_kubernetes}-contract-testing-${use_contract_testing}-observability-${use_observability}-extension-${use_extension}-messaging-${use_messaging}
                                        mkdir -p ${dirname}

                                        filename="${dirname}/options.adoc"

                                        # Write the combination to the file
                                        echo ":os: $os" >"$filename"

                                        # For the true falses combos, only define a variable if it's true
                                        if [ "$use_ai" = 'true' ]; then echo ":use-ai:" >>"$filename"; fi
                                        if [ "$use_azure" = 'true' ]; then echo ":use-azure:" >>"$filename"; fi
                                        if [ "$use_container" = 'true' ]; then echo ":use-container:" >>"$filename"; fi
                                        if [ "$use_contract_testing" = 'true' ]; then echo ":use-contract-testing:" >>"$filename"; fi
                                        if [ "$use_extension" = 'true' ]; then echo ":use-extension:" >>"$filename"; fi
                                        if [ "$use_kubernetes" = 'true' ]; then echo ":use-kubernetes:" >>"$filename"; fi
                                        if [ "$use_messaging" = 'true' ]; then echo ":use-messaging:" >>"$filename"; fi
                                        if [ "$use_native" = 'true' ]; then echo ":use-native:" >>"$filename"; fi
                                        if [ "$use_observability" = 'true' ]; then echo ":use-observability:" >>"$filename"; fi
                                        echo "Created $filename"
                                    done
                                done
                            done
                        done
                    done
                done
            done
        done
    done
done
