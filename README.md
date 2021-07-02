# QUBCoin

DG03: QUBCoin – Using Blockchain to reward Attendance and other Positive Behaviour

<img src="/app/src/main/res/drawable/qubcoin_logo_hd.png"  width="120" height="120">

## Contents
1. [Prerequisites](#prerequisites)
2. [Developer Setup](#developer-setup)
3. [Folder Structure](#folder-structure)
4. [Testing](#testing)
5. [Naming Conventions](#branching-and-versioning)
6. [JIRA Dashboard](#jira)
7. [Credit](#credit)

## Prerequisites

You will need:
- NodeJS (I recommend v12.20.1)
- Docker
- VirtualBox
- GoLang (v 1.15)
- Android Studio

## Developer Setup

#### Virtual Machine setup
- Open your favourite terminal provider
- Run `vagrant up` this will download and install the Ubuntu 18.04LTS VM and launch it on VirtualBox
- If that was a success then run `vagrant ssh`
- You should now be logged in to the virtual machine
- From here `cd network/setup/vexpress`
- Then run the script by `./init-vexpress.sh` 
- Once that is completed run `./validate-setup.sh` and read the output to ensure all is well

#### Blockchain setup
- `cd` back to the `vagrant` directory (where you started)
- Run `setup-blockchain.sh` to clean the VM and setup the blockchain

#### Webapp and SDK setup
- Open a new terminal (not within the vagrant box)
- `cd` to `middleware_app`
- Run `setup-and-launch.sh` which installs dependencies, creates user wallet and starts webserver
- Front end web server runs on `http://localhost:5000`
- To expose the web server for the Android app to see:
    - `npm install -g localtunnel` and `lt --port 5000`

Deprecated API v1 and webapp:
- `cd middleware_app/webserver`
- Run `browser-sync` to launch the front end UI available at `http://localhost:3000`
- Run `node app.js` to launch the front end web server (runs on `http://localhost:4000`)

#### Mobile App setup
- Open Android Studio on the current branch
- You will need to install gradle and android sdks (can be done within Android studio) use current version
- In order for the Mobile app to connect to locally hosted SDK API insert localtunnel url into API_URL fields `build.gradle` file
    - (In future, the middleware would be deployed on the cloud and this link would be fixed ie qubcoin.qub.ac.uk, for example)
- Download and install an android emulator or connect and setup physical android device
- Click `File > Sync Project with Gradle Files`
- You can now build the project and install on AVD or Physical hardware

## Folder Structure
- `app` This folder contains all Mobile app related source code and resources:
    * `/src/main/java` Contains all relevant Java source code with android activities, data models along with network interaction classes.
    * `/src/test` and `/src/androidTest` folders for the Android app unit tests
    * `/res` Contains all the app's resources including static resources, the `AndroidManifest.xml` file and activity layout files under `layout`

- `gocc` This folder contains all the GoLang chaincode to be installed on Hyperledger Fabric
    * Various README.md files are under each sub-folder for further guidance.

- `gradle` This folder holds the gradle wrapper config used to build the android app.

- `Meeting Minutes` Used to store the `.txt` recoring of each supervisor meeting

- `middleware_app` Portion of the app that manages the Node.js Fabric SDK and the front end RESTful API
    * `sdk` contains code which interacts directly with the blockchain via client or gateway APIs
    * `webserver` front-end API and temp/debug simple webapp

- `network` defines the hyperledger fabric instance setup and installation
    * `bin` contains all the setup and mangement shell scripts
    * `config` the network setup yaml config files
    * `crypto` dynamically generated files of ledger interactions and security
    * `devenv` HLF dev environment setup files (broken in HLF2.2)
    * `events` utilities to access HLF generated events
    * `scripts` contains the `set-env.sh` script used to set the active fabric environment
    * `setup` extensive scripts to setup the VM and install all necessary pre-reqs

- Top level dir contains key files such as gradle build config, this README and the Vagrantfile

## Testing
Unit tests for the various sub-systems within this project are located as follows:
##### Android Testing:
- Within Android studio right click on 
    - `test` and click `run tests` for local JVM/JUnit logic tests
    - `androidTest` and click `run tests` for instrumented tests (Requires hardware device or emulator)

##### Middleware Testing:
- SDK: From terminal `cd` to `sdk` and run `npm test` (this will set up all dependencies etc)
- API: From terminal `cd` to `webserver` and run `npm test` (this will set up all dependencies etc)
- Output from test will be visible on terminal or from `.nyc_output` sub-folders

##### HLF Blockchain Testing
Within vagrant terminal:
- `cd gocc/src/token/ERC20`
- `./test.sh` 
This will create a new instance of the chaincode and test within running system.

## Branching and versioning

Live branch: `master`
Development branch style `<name>/<ticket-number>-<description-of-ticket>` for example 
`dmullan/QUBCOIN-22-update-project-readme`

Version History: 
Version 0.1 on branch `QUBCOIN_0.1` completed for the interim demo week 12.
Version 0.2 on branch `QUBCOIN_0.2` marked the completion of the baseline functional requirements. (1/3/21)

## Jira 

[JIRA dashboard](https://dmullan25.atlassian.net/secure/RapidBoard.jspa?rapidView=1) 

## Credit

Most HLF and Node.js SDK code written by Rajeev from the mastering chaincode development course on [udemy](https://www.udemy.com/user/rajeev-sakhuja-2/)
