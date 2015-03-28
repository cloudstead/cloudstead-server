cloudstead-server
=================

Implements the public website for cloudstead.io and the "launcher" app to launch cloudos instances in the cloudstead hosted environment.

##### License
For personal or non-commercial use, this code is available under the [GNU Affero General Public License, version 3](https://www.gnu.org/licenses/agpl-3.0.html).
For commercial use, please contact cloudstead.io


The absolute necessities:

a target hostname

SSL Cert for the target hostname.

Wildcard SSL Cert for cloudsteads launched from here. Launched cloudsteads will not allow ssh access until the cert is replaced.

DNS: Dyn credentials or rooty config for managing your local dns. built in tinydns support.

Cloud:
AWS access/secret key
S3 bucket name - data storage for launched cloudsteads
IAM group - each launched cloudstead will be associated with an IAM user in this group
Data Key - data in S3 is encrypted with this key before writing. Note each cloudstead generates its own key when launched. This key is just for ourselves the launcher.
Cloud storage user - data in S3 will be written to this prefix. allows you to easily know/delete everything a launcher (and its cloudsteads) is storing.
Digital Ocean credentials - for launching in digitalocean

Sendgrid Credentials - launched cloudsteads get subaccounts under these credentials

Authy Creds - launched cloudsteads share this API key. this is one reason we can't allow ssh access unless the person accessing the system is the owner of the API key.

Auth Admin Creds - used for 2-factor auth on the launcher itself


Optional:

Admin login/password

Activation code (to limit access to who can register for accounts on this launcher)
