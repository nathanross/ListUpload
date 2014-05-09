#ListUpload

##Transfer a shared e-mail to google groups

If your organization used to use a single e-mail account that was accessed
by multiple people, but are moving that to a Google Groups account, Google
provides a Groups Migration api expressly for this purpose. 

## This app is also an up-to-date sample use of the Java Groups Migration API 

Google provides a Groups Migration API for multiple programming languages. 

With python they provide rather useful (if a bit hard to find) [sample code](https://github.com/google/enterprise-deployments/blob/master/apps/python/groups/test_groups_migration.py)

With Java there's  bit more difficulty. Google provides a Javadoc for
their GroupsMigration API, but it's rather sparse. Google also provides
sample code, (off of which this project is loosely based) but the sample code 
provided is out of date. (E.g. One problem is it's written using an API key login. 
To actually migrate e-mails into the group you need an Oauth2 client access token). 
There's a good afternoon's worth of trial-and-error to get it to a working state.

The class GroupsMigrationBackend.java is functionally an updated, already
usable demo of Groups Migration API, in case you aim to develop using Groups
Migration API.

# Prerequisites

To use this utility, you will need:
* JDK
* Maven
* A google apps account with a domain
* The e-mail address of the group you wish to move e-mails to.
* The IMAP details of an e-mail account you wish to transfer e-mails from.
* A google console project set up in the google api console and it's Oauth
	secrets file.
* To give permission in your Google Apps Administrator console for 
	that project to access the Groups Migration API for your Google Apps

# Build

1. Ensure you have a JDK and Maven installed, and that Maven is on your path,
	and you have the environment variable JAVA_HOME set to your JDK path.
2. Download this repository onto your computer 
3. replace appropriate sample constants in source with the authentication
	details relevant to your organization
4. mvn compile
5. mvn -q exec: java
