package org.familysearch.practice;

import org.familysearch.api.client.DiscussionState;
import org.familysearch.api.client.FamilySearchSourceDescriptionState;
import org.familysearch.api.client.PersonMatchResultsState;
import org.familysearch.api.client.PersonNonMatchesState;
import org.familysearch.api.client.ft.ChildAndParentsRelationshipState;
import org.familysearch.api.client.ft.FamilySearchFamilyTree;
import org.familysearch.api.client.ft.FamilyTreePersonState;
import org.familysearch.api.client.memories.FamilySearchMemories;
import org.familysearch.platform.ct.DiscussionReference;
import org.familysearch.platform.discussions.Discussion;
import org.gedcomx.atom.Entry;
import org.gedcomx.common.EvidenceReference;
import org.gedcomx.common.Note;
import org.gedcomx.conclusion.*;
import org.gedcomx.rs.client.*;
import org.gedcomx.rs.client.util.AncestryTree;
import org.gedcomx.rs.client.util.GedcomxPersonSearchQueryBuilder;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.source.SourceReference;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;
import org.gedcomx.types.NameType;

import javax.activation.DataSource;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.familysearch.api.client.util.FamilySearchOptions.reason;
import static org.gedcomx.rs.client.options.QueryParameter.generations;

/**
 * Created by tyganshelton on 7/16/2015.
 */
public class App {

  private String username = "";       //Put username here
  private String password = "";       //Put username here
  private String developerKey = "";   //Put username here

  private FamilySearchFamilyTree ft = null;

  private PersonState person;
  private PersonState papa;
  private PersonState mama;
  private DiscussionState discussion;
  private MemoriesUtil imageCreator;
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
    if (null != sourceRefs) {
      org.gedcomx.common.URI uri = sourceRefs.get(0).getDescriptionRef();
    }
  }
  //Runs successfully

  //Read Persona References
  public void readPersonaReferences () {
    //the person on which to read the persona references.
    PersonState person = this.person.get();

    //load the persona references for the person.
    person.loadPersonaReferences();

    //read the persona references.
    List<EvidenceReference> personaRefs = person.getPerson().getEvidence();
    if (null != personaRefs) {
      org.gedcomx.common.URI uri = personaRefs.get(0).getResource();
    }
  }
  //Runs successfully

  //Read Discussion References
  public void readDiscussionReferences () {
    //Create discussion to be read
    DiscussionState discussion = ft.addDiscussion(new Discussion().title("Unsure of gender").details("Deets"),reason("Because I said so."));
    //Attach discussion to person
    ((FamilyTreePersonState) this.person).addDiscussionReference(discussion, reason("Because I said so."));

    //the person on which to read the discussion references.
    PersonState person = this.person.get();

    //load the discussion references for the person.
    ((FamilyTreePersonState) person).loadDiscussionReferences();

    //read the discussion references.
    List<DiscussionReference> discussionRefs = person.getPerson().findExtensionsOfType(DiscussionReference.class);
    if (null != discussionRefs) {
      org.gedcomx.common.URI uri = discussionRefs.get(0).getResource();
    }
  }
  //Runs successfully

  //Read Notes
  public void readNotes () {
    //the person on which to read the notes.
    PersonState person = this.person.get();

    //create note to read
    Note note = new Note().subject("Hair color").text("Hair color is presumed peus");
    //attach note to person
    ((FamilyTreePersonState) this.person).addNote(note);

    //load the notes for the person.
    person.loadNotes();

    //read the discussion references.
    List<Note> notes = person.getPerson().getNotes();
    if  (null != notes) {
      String subject = notes.get(0).getSubject();
      String text = notes.get(0).getText();
    }
  }
  //Runs successfully

  //Read Parents, Children, or Spouses
  public void readParents () {
    //the person for which to read the parents
    PersonState person = this.person.get();   //Call PersonState.get() to repull the person's info

    PersonParentsState parents = person.readParents().ifSuccessful(); //read the parents
    if (null != parents) {
      List<Person> listOfParents = parents.getPersons();
      PersonState parentState = parents.readParent(listOfParents.get(0));
    }
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
  public void readAncestry () {
    //the person for which to read the ancestry or descendancy
    PersonState person = this.person;

    AncestryResultsState state1 = person.readAncestry(); //read the ancestry
    AncestryResultsState state2 = person.readAncestry(generations(8)); //read 8 generations of the ancestry

    PersonState ancestor = state1.readPerson(2);
    AncestryTree.AncestryNode node = state1.getTree().getAncestor(5);

    //String mothersFathersMothersName = state1.getTree().getAncestor(10).getPerson().getName().getNameForm().getFullText();
  }
  //Runs successfully

  public void readDescendency () {
    //the person for which to read the ancestry or descendancy
    PersonState person = this.papa;

    DescendancyResultsState state1 = person.readDescendancy(); //read the descendancy
    DescendancyResultsState state2 = person.readDescendancy(generations(2)); //read 2 generations of the descendancy
    //PersonState descendant = state1.readPerson("1.1.1"); ///feature request
  }
  //Runs successfully

  //Read Person Matches (i.e., Possible Duplicates)
  public void readPersonMatches () {
    //the person for which to read the matches
    PersonState person = this.person.get();

    PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();

    //iterate through the matches.
    List<Entry> entries = matches.getResults().getEntries();
    if (null != entries) {
      org.gedcomx.common.URI id = entries.get(0).getId();
    }
  }
  //Runs successfully

  //Declare Not a Match
  //Note: May not work successfully in sandbox
  public void declareNotAMatch () {
    //the match results
    PersonState person = this.person.get();
    PersonMatchResultsState matches = ((FamilyTreePersonState) person).readMatches();

    //iterate through the matches.
    List<Entry> entries = matches.getResults().getEntries();

    PersonNonMatchesState state = matches.addNonMatch(entries.get(2), reason("Because I said so."));
  }
  //Runs successfully, just not on sandbox

  //Add a Name or Fact
  public void addName () {
    //the person to which to add the name
    PersonState person = this.person.get();

    Name name = new Name("Jake Smith", new NamePart(NamePartType.Given, "Jake"), new NamePart(NamePartType.Surname, "Smith"));
    person.addName(name.type(NameType.AlsoKnownAs), reason("Because I said so.")).ifSuccessful(); //add name
  }
  //Runs successfully

  public void addFact () {
    //the person to which to add the fact.
    PersonState person = this.person;

    PersonState j = person.addFact(new Fact(FactType.Death, "1955", "Sweden"), reason("Because I said so.")).ifSuccessful(); //add death fact
  }
  //Runs successfully

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
  public void createDiscussion () {
    FamilySearchFamilyTree ft = this.ft;

    //add a discussion description
    DiscussionState discussion = ft.addDiscussion(new Discussion()
            //with a title.
            .title("What about this").details("Deets"),
        //with a change message.
        reason("Because I said so.")
    );
    this.discussion = discussion;
  }
  //Runs successfully

  //Attach a Discussion
  public void attachDiscussion () {
    //the person that will be referencing the discussion.
    PersonState person = this.person;

    DiscussionState discussion = this.discussion;

    ((FamilyTreePersonState) person).addDiscussionReference(discussion, reason("Because I said so.")); //reference the discussion.
  }
  //Runs successfully

  //Attach a Photo to a Person
  public void attachPhotoToPerson () {
    //the person to which the photo will be attached.
    PersonState person = this.person.get();

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("TweedleDum.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    //add an artifact
    SourceDescriptionState artifact = person.addArtifact(new SourceDescription()
            //with a title
            .title("Portrait of Tweedle Dum"),
        digitalImage
    );
  }
  //Runs successfully

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
  public void uploadArtifact () {
    FamilySearchMemories fsMemories = this.fsMemories;

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("Obituary.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    //add an artifact
    SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
            //with a title
            .title("Obituary for Tweedle Dum")
                //and a citation
            .citation("Generic Newspaper, 7 Aug 1955, page 1"),
        digitalImage
    );
  }
  //Runs successfully

  //Create a Memory Persona
  public void createMemoryPersona () {
    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("TweedleDum.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

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
    this.persona = persona.get();
  }
  //Runs successfully

  //Create a Persona Reference
  public void createPersonaReference () {
    //the person that will be citing the record, source, or artifact.
    PersonState person = this.person;

    //the persona that was extracted from a record or artifact.
    PersonState persona = this.persona;

    //add the persona reference.
    person.addPersonaReference(persona);
  }
  //Runs successfully

  //Attach a Photo to Multiple Persons
  public void attachPhotoToMultiplePersons () {
    //the collection to which the artifact is to be added
    CollectionState fsMemories = this.fsMemories;

    //the persons to which the photo will be attached.
    PersonState person1 = this.person;
    PersonState person2 = this.papa;
    PersonState person3 = this.mama;

    //Create unique image, because trying to upload an image identical to an existing image will return a 409 Conflict
    DataSource digitalImage = null;
    try {
      digitalImage = imageCreator.createUniqueImage("FamilyPortrait.jpg");
    } catch (IOException e) {
      e.printStackTrace();
    }

    //add an artifact
    SourceDescriptionState artifact = fsMemories.addArtifact(new SourceDescription()
            //with a title
            .title("Family of Tweedle Dum"),
        digitalImage
    );

    person1.addMediaReference(artifact); //attach to person1
    person2.addMediaReference(artifact); //attach to person2
    person3.addMediaReference(artifact); //attach to person3
  }

  public static void main(String[] args){
    App app = new App();

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
      app.readDiscussionReferences();
      app.readNotes();
      app.readParents();
      app.readChildren();
      app.readSpouses();
      app.readAncestry();
      app.readDescendency();
      app.readPersonMatches();
      app.declareNotAMatch();
      app.addName();
      app.addFact();
      app.updateName();
      app.updateGender();
      app.updateFact();
      app.createDiscussion();
      app.attachDiscussion();
      app.attachPhotoToPerson();
      app.readMemories();
      app.uploadArtifact();
      app.createMemoryPersona();
      app.createPersonaReference();
      app.readPersonaReferences();
      app.attachPhotoToMultiplePersons();   //uploading but not attaching

      app.tearDown();
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

    imageCreator = new MemoriesUtil();
  }

  //Cleans up objects used by example methods
  private void tearDown () {
    this.person.delete();
    this.papa.delete();
    this.mama.delete();
    this.discussion.delete();
    this.persona.delete();
    this.source.delete();
  }
}
