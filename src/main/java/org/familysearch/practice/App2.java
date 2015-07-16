package org.familysearch.practice;

import org.familysearch.api.client.PersonMatchResultsState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;
import org.gedcomx.atom.Entry;
import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Name;
import org.gedcomx.conclusion.NamePart;
import org.gedcomx.conclusion.Person;
import org.gedcomx.rs.client.PersonSearchResultsState;
import org.gedcomx.rs.client.PersonState;
import org.gedcomx.rs.client.util.GedcomxPersonSearchQueryBuilder;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;

import java.net.URI;
import java.util.List;

import static org.familysearch.api.client.util.FamilySearchOptions.reason;

/**
 * Created by tyganshelton on 7/16/2015.
 */
public class App2 {

  String username = "lyonrw";       //Insert username here
  String password = "1234pass";       //Insert username here
  String developerKey = "PWRD-4Y6P-8DNG-LTV9-XDD2-JBVG-R9BD-NRRP";   //Insert username here

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
      FamilySearchFamilyTree ft = this.ft;

      //put together a search query
      GedcomxPersonSearchQueryBuilder query = new GedcomxPersonSearchQueryBuilder()
          //for a John Smith
        .name("John Smith")
            //born 1/1/1900
        .birthDate("1 January 1900")
            //son of Peter.
        .fatherName("Peter Smith");

      //search the collection
      PersonSearchResultsState results = ft.searchForPersons(query);
      //iterate through the results...
      List<Entry> entries = results.getResults().getEntries();
      //read the person that was hit
      PersonState person = results.readPerson(entries.get(0));

      //search the collection for matches
      PersonMatchResultsState matches = ft.searchForPersonMatches(query);
      //iterate through the results...
      entries = results.getResults().getEntries();
      //read the person that was matched
      person = results.readPerson(entries.get(0));
    }

    //Create Person in the Family Tree
    public void createPerson () {
      FamilySearchFamilyTree ft = this.ft;

      //add a person
      PersonState person = ft.addPerson(new Person()
      //named John Smith
        .name(new Name("John Smith", new NamePart(NamePartType.Given, "John"), new NamePart(NamePartType.Surname, "Smith")))
        //male
        .gender(GenderType.Male)
        //born in chicago in 1920
        .fact(new Fact(FactType.Birth, "1 January 1920", "Chicago, Illinois"))
        //died in new york 1980
        .fact(new Fact(FactType.Death, "1 January 1980", "New York, New York")),
        //with a change message.
        reason("Because I said so.")
      );
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
//        app.readPersonByPersistentId();
        app.readPersonByFtId(true);
        app.searchForMatch();
        app.createPerson();
      } catch (Exception e) {
            e.printStackTrace();
      }
    }
}
