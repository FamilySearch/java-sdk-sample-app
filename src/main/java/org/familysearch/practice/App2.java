package org.familysearch.practice;

import org.familysearch.api.client.DiscussionState;
import org.familysearch.api.client.FamilySearchSourceDescriptionState;
import org.familysearch.api.client.PersonMatchResultsState;
import org.familysearch.api.client.PersonNonMatchesState;
import org.familysearch.api.client.ft.ChildAndParentsRelationshipState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;
import org.familysearch.api.client.memories.FamilySearchMemories;
import org.familysearch.platform.discussions.Comment;
import org.familysearch.platform.discussions.Discussion;
import org.gedcomx.atom.Entry;
import org.gedcomx.common.Note;
import org.gedcomx.conclusion.*;
import org.gedcomx.rs.client.*;
import org.gedcomx.rs.client.util.GedcomxPersonSearchQueryBuilder;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.source.SourceReference;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;
import org.gedcomx.types.NameType;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.net.URI;
import java.util.List;

import static org.familysearch.api.client.util.FamilySearchOptions.reason;

/**
 * Created by tyganshelton on 7/16/2015.
 */
public class App2 {

  private String username = "";       //Put username here
  private String password = "";       //Put username here
  private String developerKey = "";   //Put username here

  private FamilySearchFamilyTree ft = null;

  private PersonState person;
  private PersonState papa;
  private PersonState mama;
  private SourceDescriptionState source;
  private FamilySearchMemories fsMemories;
  private PersonState persona;

  //Read the FamilySearch Family Tree
  public void readFamilyTree () {
  boolean useSandbox = true; //whether to use the sandbox reference.

    //read the Family Tree
    this.ft = new FamilySearchFamilyTree(useSandbox)
      //and authenticate.
      .authenticateViaOAuth2Password(username, password, developerKey);
  }
  ///Runs successfully

  //Read a Family Tree Person by Persistent ID
  //Note: Does not work on sandbox
  public void readPersonByPersistentId () {
    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    String ark = "https://familysearch.org/ark:/61903/4:1:LCJ6-DVV";
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
  ///Runs successfully

  //Create a Couple Relationship in the Family Tree
  public void createCouple () {
      FamilySearchFamilyTree ft = this.ft;

      PersonState husband = this.papa;
      PersonState wife = this.mama;

      RelationshipState coupleRelationship = ft.addSpouseRelationship(husband, wife, reason("Because I said so.")).ifSuccessful();
  }
  ///Runs successfully

  //Create a Child-and-Parents Relationship in the Family Tree
  public void createChildParent () {
      FamilySearchFamilyTree ft = this.ft;

      PersonState father = this.papa;
      PersonState mother = this.mama;
      PersonState child = this.person;

      ChildAndParentsRelationshipState chap = ft.addChildAndParentsRelationship(child, father, mother, reason("Because I said so."));
  }
  ///Runs successfully

  //Create a Source
  public void createSource () {
    FamilySearchFamilyTree ft = this.ft;

    //add a source description
    SourceDescriptionState source = ft.addSourceDescription(new SourceDescription()
      //about some resource.
      .about(org.gedcomx.common.URI.create("https://sandbox.familysearch.org/ark:/61903/4:1:KWHL-3TP"))
      //with a title.
      .title("Birth Certificate for Jack Sprat")
      //and a citation
      .citation("Citation for the birth certificate")
      //and a note
      .note(new Note().text("Some note for the source.")),
      //with a change message.
      reason("Because I said so.")
    );
    this.source = source;
  }
  //Runs successfully

  //Create a Source Reference
  public void createSourceReference () {
    //the person that will be citing the record, source, or artifact.
      PersonState person = this.person.get();

      SourceDescriptionState source = this.source;

      person.addSourceReference(source, reason("Because I said so.")).ifSuccessful(); //cite the source.
  }
  //Runs successfully

  //Read Everything Attached to a Source
  public void readSource () {
    //the source.
      SourceDescriptionState source = this.source.get();

      SourceDescriptionState attachedReferences = ((FamilySearchSourceDescriptionState) source).queryAttachedReferences().ifSuccessful();

      //iterate through the persons attached to the source
      List<Person> persons = attachedReferences.getEntity().getPersons();
  }
  //Runs successfully

  //Read Person for the Current User
  public void readPersonForCurrentUser () {
    FamilySearchFamilyTree ft = this.ft;

    PersonState person = ft.readPersonForCurrentUser();
  }
  //Runs successfully

  //Read Source References
  public void readSourceReferences () {
    //the person on which to read the source references.
      PersonState person = this.person.get();

      //load the source references for the person.
      person.loadSourceReferences();

      //read the source references.
      List<SourceReference> sourceRefs = person.getPerson().getSources();
  }
  //Runs successfully

  //Read Persona References
  //TODO: Create persona references to be read
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
  //TODO: Create Discussion to read-currently returning 400
  public void readDiscussionReferences () {
    //Create discussion to be read
    DiscussionState d = ft.addDiscussion(new Discussion().title("Unsure of gender").comment(new Comment("I can't tell of this person is male or female")));
    //Attach discussion to person
    PersonState person = this.person.get();


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
  //TODO: Create notes to read
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
    PersonState person = this.person.get();   //Call PersonState.get() to repull the person's info

    PersonParentsState parents = person.readParents().ifSuccessful(); //read the parents
  }
  //Runs successfully

  public void readChildren () {
    //the person for which to read the children
    PersonState person = this.papa.get();

    PersonChildrenState children = person.readChildren(); //read the children
    List<Person> persons = children.getPersons();
  }
  //Runs successfully

  public void readSpouses () {
    //the person for which to read the spouses
    PersonState person = this.mama.get();

    PersonSpousesState spouses = person.readSpouses(); //read the spouses
    List<Person> persons = spouses.getPersons();
  }
  //Runs successfully

  //Read Ancestry or Descendancy
  //TODO: fix-generations(8) not recognized
  public void readAncestry () {
  //the person for which to read the ancestry or descendancy
//    PersonState person = this.person;
//
//    person.readAncestry(); //read the ancestry
//    person.readAncestry(generations(8)); //read 8 generations of the ancestry
  }

  //TODO: fix-generations(8) not recognized
  public void readDescendency () {
////the person for which to read the ancestry or descendancy
//      PersonState person = this.papa;
//
//      person.readDescendancy(); //read the descendancy
//      person.readDescendancy(generations(3)); //read 3 generations of the descendancy
  }

  //Read Person Matches (i.e., Possible Duplicates)
  public void readPersonMatches () {
    //the person for which to read the matches
    PersonState person = this.person.get();

    PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();

    //iterate through the matches.
    List<Entry> entries = matches.getResults().getEntries();
  }
  //Runs successfully

  //Declare Not a Match
  //TODO: fix-Does not affect possible duplicate and returns null
  public void declareNotAMatch () {
    //the match results
    PersonState person = this.person.get();
    PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();

    //iterate through the matches.
    List<Entry> entries = matches.getResults().getEntries();

    PersonNonMatchesState state = matches.addNonMatch(entries.get(2), reason("Because I said so.")).ifSuccessful();
  }

  //Add a Name or Fact
  //TODO: Fix-"Requested feature 'birth-date-not-considered-death-declaration' is not defined."
  public void addName () {
    //the person to which to add the name
      PersonState person = this.person.get();

      Name name = new Name("Billy Goat");
      person.addName(name.type(NameType.AlsoKnownAs), reason("Because I said so.")); //add name
  }

  public void addFact () {
      //the person to which to add the fact.
      PersonState person = this.person;

      PersonState j = person.addFact(new Fact(FactType.Death, "1955", "Sweden"), reason("Because I said so.")).ifSuccessful(); //add death fact
  }
  //Runs successfully

 //Add gender? Maybe not necessary

  //Update a Name, Gender, or Fact
  public void updateName () {
    //the person to which to update the name.
    PersonState person = this.person.get();

    Name name = person.getName();
    name.getNameForm().setFullText("Tweedle Dum");
    person.updateName(name, reason("Because I said so.")); //update name
  }
  //Runs successfully

  public void updateGender () {
    //the person to which to update the gender.
    PersonState person = this.person.get();

    Gender gender = person.getGender();
    gender.setKnownType(GenderType.Female);
    person.updateGender(gender, reason("Because I said so.")); //update gender
  }
  //Runs successfully

  public void updateFact () {
    //the person to which to update the fact.
    PersonState person = this.person.get();

    Fact death = person.getPerson().getFirstFactOfType(FactType.Death);
    death.setDate(new Date().original("1985"));
    person.updateFact(death, reason("Because I said so."));
  }
  //Runs successfully

  //Create a Discussion
  //TODO: Fix-Returns 400 "Requested feature 'birth-date-not-considered-death-declaration' is not defined."
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
  //TODO: after createDiscussion
  public void attachDiscussion () {
    //the person that will be referencing the discussion.
//      PersonState person = ...;
//
//      DiscussionState discussion = ...;
//
//      ((FamilyTreePersonState) person).addDiscussionReference(discussion, reason("Because I said so.")); //reference the discussion.
  }

  //Attach a Photo to a Person
  //TODO: fix-without .ifSuccessful, returns 409 Conflict. With, causes birth-date-not-considered-death-date error
  public void attachPhotoToPerson () {
    //the person to which the photo will be attached.
    PersonState person = this.person.get();
    DataSource digitalImage = new FileDataSource("TweedleDum.jpg");

    //add an artifact
    SourceDescriptionState artifact = person.addArtifact(new SourceDescription()
            //with a title
            .title("Portrait of Tweedle Dum"),
        digitalImage
    );
  }
  //WAS running successfully

  //Read FamilySearch Memories
  //More complete sample: Find a specific memory
  public void readMemories () {
    boolean useSandbox = true; //whether to use the sandbox reference.
    String username = this.username;
    String password = this.password;
    String developerKey = this.developerKey;

    //read the Family Tree
    FamilySearchMemories fsMemories = new FamilySearchMemories(useSandbox)
      //and authenticate.
      .authenticateViaOAuth2Password(username, password, developerKey);
    this.fsMemories = fsMemories;
  }
  //Runs successfully

  //Upload Photo or Story or Document
  //TODO: fix-returned 409: "Requested feature 'birth-date-not-considered-death-declaration' is not defined."
  public void uploadArtifact () {
      FamilySearchMemories fsMemories = this.fsMemories;
      DataSource digitalImage = new FileDataSource("Obituary.jpg");

      //add an artifact
      SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
        //with a title
        .title("Obituary for Tweedle Dum")
        //and a citation
        .citation("Generic Newspaper, 7 Aug 1955, page 1"),
        digitalImage
      );
  }

  //Create a Memory Persona
  //TODO: Does sandbox not support adding artifacts?
  public void createMemoryPersona () {
    DataSource digitalImage = new FileDataSource("TweedleDum.jpg");
    PersonState person = this.person.get();
    //the artifact from which a persona will be extracted.
    SourceDescriptionState artifact = person.addArtifact(new SourceDescription()
            .title("Portrait of Tweedle Dee Dum"),
        digitalImage
    );

    //add the persona
    PersonState persona = artifact.addPersona(new Person()
        //named Tweedle Dee Dum
        .name("Tweedle Dee Dum"));
    this.persona = persona;
  }

  //Create a Persona Reference
  //TODO: after createMemoryPersona
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
  //TODO: after attachPhoto
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
      app.setUp();
      app.readPersonByFtId(true);
      app.searchForMatch();
      app.createPerson();
      app.createCouple();
      app.createChildParent();
      app.createSource();
      app.createSourceReference();
      app.readSource();
      app.readPersonForCurrentUser();
      app.readSourceReferences();
      //app.readPersonaReferences();          //Create persona references to be read
      //app.readDiscussionReferences();       //Create discussions to read
      //app.readNotes();                      //Create notes to read
      app.readParents();
      app.readChildren();
      app.readSpouses();
      //app.readAncestry();                   //fix-generations(8) not recognized
      //app.readDescendency();                //fix-generations(8) not recognized
      app.readPersonMatches();
      //app.declareNotAMatch();               //Does not affect possible duplicate and returns null
      //app.addName();                        //returns "Requested feature 'birth-date-not-considered-death-declaration' is not defined."
      app.addFact();
      app.updateName();
      app.updateGender();
      app.updateFact();
      //app.createDiscussion();               //returns 400 "Requested feature 'birth-date-not-considered-death-declaration' is not defined
      //app.attachDiscussion();               //complete after createDiscussion() works
      //app.attachPhotoToPerson();            //returns 409 Conflict: "Requested feateure 'birth-date-not-considered-death-date' is not defined."
      app.readMemories();
      //app.uploadArtifact();                 //returns 409: "Requested feature 'birth-date-not-considered-death-declaration' is not defined."
      //app.createMemoryPersona();            //complete after createMemoryPersona() works
      //app.createPersonaReference();         //complete after createMemoryPersona() works
      //app.attachPhotoToMultiplePersons();   //complete after attachPhoto() works
    } catch (Exception e) {
          e.printStackTrace();
    }
  }

  //Sets up objects to be used by example methods
  private void setUp () {
    //Used as person and child
    this.person = ft.addPerson(new Person()
            .name(new Name("Jack Sprat", new NamePart(NamePartType.Given, "Jack"), new NamePart(NamePartType.Surname, "Sprat")).preferred(true))
            .gender(GenderType.Male)
            .fact(new Fact(FactType.Birth, "1 January 1890", "Chicago, Illinois"))
            .fact(new Fact(FactType.Death, "1 January 1970", "New York, New York")),
        reason("Because I said so.")
    ).ifSuccessful();

    //Used as husband and father
    this.papa = ft.addPerson(new Person()
            .name(new Name("Papa Moose", new NamePart(NamePartType.Given, "Papa"), new NamePart(NamePartType.Surname, "Moose")).preferred(true))
            .gender(GenderType.Male)
            .fact(new Fact(FactType.Birth, "1 January 1865", "Chicago, Illinois"))
            .fact(new Fact(FactType.Death, "1 January 1945", "New York, New York")),
        reason("Because I said so.")
    ).ifSuccessful();

    //Used as wife and mother
    this.mama = ft.addPerson(new Person()
            .name(new Name("Mother Goose", new NamePart(NamePartType.Given, "Mother"), new NamePart(NamePartType.Surname, "Goose")).preferred(true))
            .gender(GenderType.Female)
            .fact(new Fact(FactType.Birth, "1 January 1865", "Chicago, Illinois"))
            .fact(new Fact(FactType.Death, "1 January 1945", "New York, New York")),
        reason("Because I said so.")
    ).ifSuccessful();


  }

  //Cleans up objects used by example methods
  private void tearDown () {
  }
}
