package org.familysearch.practice;

import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;

import java.net.URI;

/**
 * Created by tyganshelton on 7/16/2015.
 */
public class App2 {

  String username = "";       //Insert username here
  String password = "";       //Insert username here
  String developerKey = "";   //Insert username here

  FamilySearchFamilyTree ft = null;

  //Read the FamilySearch Family Tree
  public void readFamilyTree () {
  boolean useSandbox = true; //whether to use the sandbox reference.

    //read the Family Tree
    this.ft = new FamilySearchFamilyTree(useSandbox)
      //and authenticate.
      .authenticateViaOAuth2Password(username, password, developerKey);
    }

    ///Returns 401 because ft is not sandbox. It's not obvious how to make it sandbox
    //Read a Family Tree Person by Persistent ID
    public void readPersonByPersistentId () {
      String username = this.username;
      String password = this.password;
      String developerKey = this.developerKey;

      String ark = "https://sandbox.familysearch.org/ark:/61903/4:1:KW41-FDB";
      FamilyTreePersonState person = new FamilyTreePersonState(URI.create(ark))
        .authenticateViaOAuth2Password(username, password, developerKey);
    }

    //Read a Family Person by Family Tree ID, with or without relationships
    public void readPersonByFtId (boolean withRelationships) {
      String pid = "KW41-FDB";

      FamilySearchFamilyTree ft = this.ft;

      FamilyTreePersonState person = null;
      if(withRelationships){
        person = ft.readPersonWithRelationshipsById(pid);
      }
      else {
        person = ft.readPersonById(pid);
      }
    }

    //Search for Persons or Person Matches in the Family Tree
    public void searchForMatch () {

    }

    //Create Person in the Family Tree
    public void createPerson () {

    }

    //Create a Couple Relationship in the Family Tree
    public void createCouple () {

    }

    //Create a Child-and-Parents Relationship in the Family Tree
    public void createChildParent () {

    }

    //Create a Source
    public void createSource () {

    }

    //Create a Source Reference
    public void createSourceReference () {

    }

    //Read Everything Attached to a Source
    public void readSource () {

    }

    //Read Person for the Current User
    public void readPersonForCurrentUser () {

    }

    //Read Source References
    public void readSourceReferences () {

    }

    //Read Persona References
    public void readPersonaReferences () {

    }

    //Read Discussion References
    public void readDiscussionReferences () {

    }

    //Read Notes
    public void readNotes () {

    }

    //Read Parents, Children, or Spouses
    public void readParents () {

    }

    public void readChildren () {

    }

    public void readSpouses () {

    }

    //Read Ancestry or Descendancy
    public void readAncestry () {

    }

    public void readDescendency () {

    }

    //Read Person Matches (i.e., Possible Duplicates
    public void readPersonMatches () {

    }

    //Declare Not a Match
    public void declareNotAMatch () {

    }

    //Add a Name or Fact
    public void addName () {

    }

    public void addFact () {

    }

    //Update a Name, Gender, or Fact
    public void updateName () {

    }

    public void updateGender () {

    }

    public void updateFact () {

    }

    //Create a Discussion
    public void createDiscussion () {

    }

    //Attach a Discussion
    public void attachDiscussion () {

    }

    //Attach a Photo to a Person
    public void attachPhotoToPerson () {

    }

    //Read FamilySearch Memories
    public void readMemories () {

    }

    //Upload Photo or Story or Document
    public void uploadArtifact () {

    }

    //Create a Memory Persona
    public void createMemoryPersona () {

    }

    //Create a Persona Reference
    public void createPersonaReference () {

    }

    //Attach a Photo to Multiple Persons
    public void attachPhotoToMultiplePersons () {

    }


    public static void main(String[] args){
      App2 app = new App2();

      try {
        app.readFamilyTree();
        app.readPersonByFtId(false);
      } catch (Exception e) {
            e.printStackTrace();
      }
    }
}
