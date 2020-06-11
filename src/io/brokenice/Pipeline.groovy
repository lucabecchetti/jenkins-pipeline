#!/usr/bin/groovy
package io.brokenice;
///////////////////////////////////////////////////////////////////////////////
//                   JENKINS PIPELINE TO KUBERNETES DEPLOY
// Title:            jenkins-pipeline
//
// Author:           Luca Becchetti
// Email:            luca.becchetti@brokenice.it
//
///////////////////////////////////////////////////////////////////////////////

/**
 * Test that kubectl can correctly communication with the Kubernetes API
 *
 * @return void
 */
def kubectlTest() {
    println "checking kubectl connnectivity to the API"
    sh "kubectl get nodes"

}

/**
 * Lint helm chart
 *
 * @return void
 */
def helmLint(String chart_dir) {
    println "running helm lint ${chart_dir}"
    sh "helm lint ${chart_dir}"

}

/**
 * Check helm version installed
 *
 * @return void
 */
def helmConfig() {
    println "checking client/server version"
    sh "helm version"
}


/**
 * Deploy helm chart passed by parameters

 * @param void
 * @return void
 */
def helmDeploy(Map args) {

    //helmConfig()

    def String namespace

    // If namespace isn't parsed into the function set the namespace to the name
    if (args.namespace == null) {
        namespace = args.name
    } else {
        namespace = args.namespace
    }

    // Check for dryRun or for real deployment
    if (args.dry_run) {
        println "Running dry-run deployment"
        sh "helm upgrade --dry-run --install ${args.name} ${args.chart_dir} --set imageTag=${args.version_tag} --namespace=${namespace}"
    } else {
        println "Running deployment"

        // reimplement --wait once it works reliable
        sh "helm upgrade --install ${args.name} ${args.chart_dir} --set imageTag=${args.version_tag} --namespace=${namespace}"

        // sleeping until --wait works reliably
        sleep(20)

        echo "Application ${args.name} successfully deployed. Use helm status ${args.name} to check"
    }
}

/**
 * Delete an helm chart deployed on cluster

 * @param args
 * @return void
 */
def helmDelete(Map args) {
        println "Running helm delete ${args.name}"

        sh "helm delete ${args.name}"
}

/**
 * Running helm test 

 * @param args
 * @return void
 */
def helmTest(Map args) {
    println "Running Helm test"

    sh "helm test ${args.name} --cleanup"
}

/**
 * Create git envvars variables (GIT_COMMIT_ID, GIT_SHA, GIT_REMOTE_URL) from current git repo

 * @param args
 * @return void
 */
def gitEnvVars() {
    
    println "Setting envvars to tag container"

    sh 'git rev-parse HEAD > git_commit_id.txt'
    try {
        env.GIT_COMMIT_ID = readFile('git_commit_id.txt').trim()
        env.GIT_SHA = env.GIT_COMMIT_ID.substring(0, 7)
    } catch (e) {
        error "${e}"
    }
    println "env.GIT_COMMIT_ID ==> ${env.GIT_COMMIT_ID}"

    sh 'git config --get remote.origin.url> git_remote_origin_url.txt'
    try {
        env.GIT_REMOTE_URL = readFile('git_remote_origin_url.txt').trim()
    } catch (e) {
        error "${e}"
    }
    println "env.GIT_REMOTE_URL ==> ${env.GIT_REMOTE_URL}"
}