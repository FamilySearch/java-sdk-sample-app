# SampleApp
Sample Application of FamilySearch API SDK

This project demonstrates how to use the FamilySearch API and SDK. It offers examples of the
documentation provided at https://github.com/FamilySearch/gedcomx-java/tree/master/extensions/familysearch/familysearch-api-client

This repository includes two runnable classes:

1.  App.java: This class shows step-by-step samples of each example given in the documentation.

2.  PersonStateExample.java: This class shows how to use methods of PersonState objects not described in
    the documentation.

To successfully run SampleApp:

1.  Clone the repository at https://github.com/tygan/SampleApp.git

2.  Add the SampleApp project as a Maven project. In Intellij, it will prompt you to do so.

3.  Set up the SDK as 1.7. In Intellij, it will prompt you to do so.

4.  In MemoriesUtil.java, set the language level to 6 to compile @Override in interfaces. In Intellij,
    it will prompt you do so.

5.  Set the target release to 1.6. In Intellij, you can do this by going to Settings, going to
    "Java Compiler," and changing the version under "Target bytecode version" to "1.6"

6.  Run either main class

7.  As prompted, provide a valid username, password, and developer key.