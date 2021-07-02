# Updated: April 2020
# The node code MUST be executed in HOST machine NOT in VM
# Please ensure to install Node version > 12.3.6 on the host machine

# To setup SDK
cd sdk
npm install

# To setup and run webserver
cd webserver
npm install
node appv2.js

# To test webserver blockchain VM must be up and working correctly
cd webserver
npm test
