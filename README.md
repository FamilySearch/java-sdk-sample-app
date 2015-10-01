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
2.  Add the SampleApp project as a Maven project. In Intellij, you can do this by right-clicking on the pom.xml and selecting "Add as Maven project" in the drop-down menu.
3.  Setup the SDK as 1.7. In Intellij, you can do this by opening the Project Structure dialog with Ctrl+Shift+Alt+S, selecting 1.7 under Project SDK in the Project tab, and clicking "OK."
4.  Setup the target release to 1.6. In Intellij, you can do this by going to Settings, going to
    "Java Compiler," and changing the version under "Target bytecode version" to "1.6".
5.  Run either App or PersonStateExample.
6.  As prompted, provide a valid username, password, and developer key.
