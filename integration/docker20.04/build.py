from os import environ
from pybuilder.core import task, init
from ddadevops import *
import logging

name = 'dda-serverspec-20.04'
MODULE = 'image'
PROJECT_ROOT_PATH = '../..'

class MyBuild(DevopsDockerBuild):
    pass

from subprocess import run
@init
def initialize(project):
    project.build_depends_on('ddadevops>=0.6.1')
    stage = 'notused'
    dockerhub_user = 'notused'
    dockerhub_password = 'notused'
    config = create_devops_docker_build_config(
        stage, PROJECT_ROOT_PATH, MODULE, dockerhub_user, dockerhub_password)
    build = MyBuild(project, config)
    build.initialize_build_dir()
    run('cp -r ' + PROJECT_ROOT_PATH + '/integration/resources ' +
        build.build_path() + '/' + MODULE, shell=True)
    run('cp ' + PROJECT_ROOT_PATH + '/target/uberjar/dda-serverspec-standalone.jar ' + 
        build.build_path() + '/' + MODULE + '/', shell=True)

@task
def image(project):
    build = get_devops_build(project)
    build.image()

@task
def drun(project):
    build = get_devops_build(project)
    build.drun()

@task
def test(project):
    build = get_devops_build(project)
    build.test()