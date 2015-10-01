# FamilySearch API Java SDK Sample App

An application that demonstrates the usage of the [Java SDK](https://github.com/FamilySearch/gedcomx-java/tree/master/extensions/familysearch/familysearch-api-client) library for the FamilySearch API.

The sample app requires a user account in the sandbox reference of the FamilySearch Family Tree. A sandbox account is obtained when registering with the [developer community](https://grms.force.com/Developer).

##Description

This repository includes two runnable classes:

1.  App.java: This class shows step-by-step samples of each example given in the documentation.
2.  PersonStateExample.java: This class shows how to use methods of PersonState objects not described in
    the documentation.

## Installation

Requirements:

* git
* Java 1.7

Steps:

1.  Clone the repository at https://github.com/tygan/SampleApp.git
2.  Add the SampleApp project as a Maven project. In Intellij, it will prompt you to do so.
3.  Ensure the SDK is set up as 1.7. In Intellij, it will prompt you to do so when you open App.java.
4.  If needed, set the target release to 1.6. In Intellij, you can do this by going to Settings, going to
    "Java Compiler," and changing the version under "Target bytecode version" to "1.6".
5.  Run either App or PersonStateExample.
6.  As prompted, provide a valid username, password, and developer key.
