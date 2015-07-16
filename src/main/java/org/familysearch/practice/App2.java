package org.familysearch.practice;

import org.familysearch.api.client.DiscussionState;
import org.familysearch.api.client.PersonMatchResultsState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;
import org.familysearch.api.client.memories.FamilySearchMemories;
import org.familysearch.platform.discussions.Discussion;
import org.gedcomx.atom.Entry;
import org.gedcomx.common.Note;
import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Name;
import org.gedcomx.conclusion.NamePart;
import org.gedcomx.conclusion.Person;
import org.gedcomx.rs.client.PersonSearchResultsState;
import org.gedcomx.rs.client.PersonState;
import org.gedcomx.rs.client.SourceDescriptionState;
import org.gedcomx.rs.client.util.GedcomxPersonSearchQueryBuilder;
import org.gedcomx.source.SourceDescription;
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
  ///Runs successfully

  //TODO: Fix-Returns 401 because ft is not sandbox. It's not obvious how to make it sandbox
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
  ///Runs successfully

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
  ///Runs successfully

  //TODO: Fix-returns 400 for unknown reason
  //Create Person in the Family Tree
  public void createPerson () {
    FamilySearchFamilyTree ft = this.ft;

    //add a person
    PersonState person = ft.addPerson(new Person()
    //named John Smith
      .name(new Name("John Smith", new NamePart(NamePartType.Given, "John"), new NamePart(NamePartType.Surname, "Smith")).preferred(true))
      //male
      .gender(GenderType.Male)
      //born in chicago in 1920
      .fact(new Fact(FactType.Birth, "1 January 1920", "Chicago, Illinois"))
      //died in new york 1980
      .fact(new Fact(FactType.Death, "1 January 1980", "New York, New York")),
      //with a change message.
      reason("Because I said so.")
    ).ifSuccessful();
  }

  //Create a Couple Relationship in the Family Tree
  public void createCouple () {
//      FamilySearchFamilyTree ft = this.ft;
//
//      PersonState husband = ...;
//      PersonState wife = ...;
//
//      RelationshipState coupleRelationship = ft.addSpouseRelationship(husband, wife, reason("Because I said so."));
  }

  //Create a Child-and-Parents Relationship in the Family Tree
  public void createChildParent () {
//      FamilySearchFamilyTree ft = this.ft;
//
//      PersonState father = ...;
//      PersonState mother = ...;
//      PersonState child = ...;
//
//      ChildAndParentsRelationshipState chap = ft.addChildAndParentsRelationship(child, father, mother, reason("Because I said so."));
  }

  //Create a Source
  public void createSource () {
    FamilySearchFamilyTree ft = this.ft;

    //add a source description
    SourceDescriptionState source = ft.addSourceDescription(new SourceDescription()
      //about some resource.
      .about(org.gedcomx.common.URI.create("http://familysearch.org/ark:/..."))
      //with a title.
      .title("Birth Certificate for John Smith")
      //and a citation
      .citation("Citation for the birth certificate")
      //and a note
      .note(new Note().text("Some note for the source.")),
      //with a change message.
      reason("Because I said so.")
    );
  }

  //Create a Source Reference
  public void createSourceReference () {
    //the person that will be citing the record, source, or artifact.
//      PersonState person = ...;
//
//      SourceDescriptionState source = ...;
//
//      person.addSourceReference(source, reason("Because I said so.")); //cite the source.
  }

  //Read Everything Attached to a Source
  public void readSource () {
    //the source.
//      SourceDescriptionState source = ...;
//
//      SourceDescriptionState attachedReferences = ((FamilySearchSourceDescriptionState) source).queryAttachedReferences();
//
//      //iterate through the persons attached to the source
//      List<Person> persons = attachedReferences.getEntity().getPersons();
  }

  //Read Person for the Current User
  public void readPersonForCurrentUser () {
    FamilySearchFamilyTree ft = this.ft;

    PersonState person = ft.readPersonForCurrentUser();
  }

  //Read Source References
  public void readSourceReferences () {
    //the person on which to read the source references.
//      PersonState person = ...;
//
//      //load the source references for the person.
//      person.loadSourceReferences();
//
//      //read the source references.
//      List<SourceReference> sourceRefs = person.getPerson().getSources();
  }

    //Read Persona References
  public void readPersonaReferences () {
    //the person on which to read the persona references.
//      PersonState person = ...;
//
//      //load the persona references for the person.
//      person.loadPersonaReferences();
//
//      //read the persona references.
//      List<EvidenceReference> personaRefs = person.getPerson().getEvidence();
  }

  //Read Discussion References
  public void readDiscussionReferences () {
    //the person on which to read the discussion references.
//      PersonState person = ...;
//
//      //load the discussion references for the person.
//      ((FamilyTreePersonState) person).loadDiscussionReferences();
//
//      //read the discussion references.
//      List<DiscussionReference> discussionRefs = person.getPerson().findExtensionsOfType(DiscussionReference.class);
  }

  //Read Notes
  public void readNotes () {
//      //the person on which to read the notes.
//      PersonState person = ...;
//
//      //load the notes for the person.
//      person.loadNotes();
//
//      //read the discussion references.
//      List<Note> notes = person.getPerson().getNotes();
  }

  //Read Parents, Children, or Spouses
  public void readParents () {
    //the person for which to read the parents
//      PersonState person = ...;
//
//      PersonParentsState parents = person.readParents(); //read the parents
  }

  public void readChildren () {
    //the person for which to read the children
//      PersonState person = ...;
//
//      PersonChildrenState children = person.readChildren(); //read the children
  }

  public void readSpouses () {
    //the person for which to read the spouses
//      PersonState person = ...;
//
//      PersonSpousesState spouses = person.readSpouses(); //read the spouses
  }

  //Read Ancestry or Descendancy
  public void readAncestry () {
////the person for which to read the ancestry or descendancy
//      PersonState person = ...;
//
//      person.readAncestry(); //read the ancestry
//      person.readAncestry(generations(8)); //read 8 generations of the ancestry
  }

  public void readDescendency () {
////the person for which to read the ancestry or descendancy
//      PersonState person = ...;
//
//      person.readDescendancy(); //read the descendancy
//      person.readDescendancy(generations(3)); //read 3 generations of the descendancy
  }

  //Read Person Matches (i.e., Possible Duplicates
  public void readPersonMatches () {
    //the person for which to read the matches
//      PersonState person = ...;
//
//      PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();
//
//      //iterate through the matches.
//      List<Entry> entries = matches.getResults().getEntries();
  }

  //Declare Not a Match
  public void declareNotAMatch () {
    //the match results
//      PersonMatchResultsState matches = ...;
//
//      //iterate through the matches.
//      List<Entry> entries = matches.getResults().getEntries();
//
//      matches.addNonMatch(entries.get(2), reason("Because I said so."));
  }

  //Add a Name or Fact
  public void addName () {
    //the person to which to add the name
//      PersonState person = ...;
//
//      Name name = ...;
//      person.addName(name.type(NameType.AlsoKnownAs), reason("Because I said so.")); //add name
  }

  public void addFact () {
      //the person to which to add the fact.
//      PersonState person = ...;
//
//      person.addFact(new Fact(FactType.Death, "date", "place"), reason("Because I said so.")); //add death fact
  }

  //add gender

  //Update a Name, Gender, or Fact
  public void updateName () {
    //the person to which to update the name.
//      PersonState person = ...;
//
//      Name name = person.getName();
//      name.getNameForm().setFullText("Joanna Smith");
//      person.updateName(name, reason("Because I said so.")); //update name
  }

  public void updateGender () {
      //the person to which to update the gender.
//      PersonState person = ...;
//
//      Gender gender = person.getGender();
//      gender.setKnownType(GenderType.Female);
//      person.updateGender(gender, reason("Because I said so.")); //update gender
  }

  public void updateFact () {
      //the person to which to update the fact.
//      PersonState person = ...;
//
//      Fact death = person.getPerson().getFirstFactOfType(FactType.Death);
//      death.setDate(new Date().original("new date"));
//      person.updateFact(death, reason("Because I said so."));
  }

  //Create a Discussion
  public void createDiscussion () {
    FamilySearchFamilyTree ft = this.ft;

    //add a discussion description
    DiscussionState discussion = ft.addDiscussion(new Discussion()
      //with a title.
      .title("What about this"),
      //with a change message.
      reason("Because I said so.")
    );
  }

  //Attach a Discussion
  public void attachDiscussion () {
    //the person that will be referencing the discussion.
//      PersonState person = ...;
//
//      DiscussionState discussion = ...;
//
//      ((FamilyTreePersonState) person).addDiscussionReference(discussion, reason("Because I said so.")); //reference the discussion.
  }

  //Attach a Photo to a Person
  public void attachPhotoToPerson () {
    //the person to which the photo will be attached.
//      PersonState person = ...;
//      DataSource digitalImage = new FileDataSource("/path/to/img.jpg");
//
//      //add an artifact
//      SourceDescriptionState artifact = person.addArtifact(new SourceDescription()
//        //with a title
//        .title("Portrait of John Smith"),
//        digitalImage
//      );
  }

  //Read FamilySearch Memories
  public void readMemories () {
    boolean useSandbox = true; //whether to use the sandbox reference.
    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    //read the Family Tree
    FamilySearchMemories fsMemories = new FamilySearchMemories(useSandbox)
      //and authenticate.
      .authenticateViaOAuth2Password(username, password, developerKey);
  }

  //Upload Photo or Story or Document
  public void uploadArtifact () {
//      FamilySearchMemories fsMemories = ...;
//      DataSource digitalImage = new FileDataSource("/path/to/img.jpg");
//
//      //add an artifact
//      SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
//        //with a title
//        .title("Death Certificate for John Smith")
//        //and a citation
//        .citation("Citation for the death certificate"),
//        digitalImage
//      );
  }

  //Create a Memory Persona
  public void createMemoryPersona () {
    //the artifact from which a persona will be extracted.
//      SourceDescriptionState artifact = ...;
//
//      //add the persona
//      PersonState persona = artifact.addPersona(new Person()
//        //named John Smith
//        .name("John Smith"));
  }

  //Create a Persona Reference
  public void createPersonaReference () {
    //the person that will be citing the record, source, or artifact.
//      PersonState person = ...;
//
//      //the persona that was extracted from a record or artifact.
//      PersonState persona = ...;
//
//      //add the persona reference.
//      person.addPersonaReference(persona);
  }

  //Attach a Photo to Multiple Persons
  public void attachPhotoToMultiplePersons () {
    //the collection to which the artifact is to be added
//      CollectionState fsMemories = ...;
//
//      //the persons to which the photo will be attached.
//      PersonState person1 = ...;
//      PersonState person2 = ...;
//      PersonState person3 = ...;
//      DataSource digitalImage = new FileDataSource("/path/to/img.jpg");
//
//      //add an artifact
//      SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
//        //with a title
//        .title("Family of John Smith"),
//        digitalImage
//      );
//
//      person1.addMediaReference(artifact); //attach to person1
//      person2.addMediaReference(artifact); //attach to person2
//      person3.addMediaReference(artifact); //attach to person3
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
