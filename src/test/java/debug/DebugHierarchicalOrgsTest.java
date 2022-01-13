package debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DebugHierarchicalOrgsTest {

  private static final Logger logger = LoggerFactory.getLogger(DebugHierarchicalOrgsTest.class);

  private static int TEST_NUMBER;

  @Autowired
  OrganizationManager manager;

  UUID child1Id;
  UUID child2Id;
  UUID parentId;
  UUID otherParentId;

  @BeforeAll
  static void checkModel() throws Exception {
    Method method = Organization.class.getMethod("getOrganizations");

    assertThat(method.getReturnType()).isAssignableFrom(List.class);

    TEST_NUMBER = 0;
  }

  @BeforeEach
  void prepareEntities(final TestInfo testInfo) throws Exception {
    logger.warn("Starting test '{}'", testInfo.getDisplayName());
    TEST_NUMBER++;

    Organization child1 = manager.create(newOrg("Child 1"));
    child1Id = child1.getId();
    Organization child2 = manager.create(newOrg("Child 2"));
    child2Id = child2.getId();

    // creating a parent with child orgs
    Organization parent = manager.create(newOrg("Parent", child1, child2));
    parentId = parent.getId();

    Organization otherParent = manager.create(newOrg("New Parent"));
    otherParentId = otherParent.getId();

    assertAll(() -> assertThat(Arrays.asList(child1Id, child2Id, otherParentId, parentId)).doesNotContainNull(),
        () -> assertThat(parent.getOrganizations()).containsExactly(child1, child2),
        () -> assertThat(otherParent.getOrganizations()).isEmpty());

    logger.info("Created test data");
  }

  @AfterEach
  void deleteEntities(final TestInfo testInfo) {
    for (UUID orgId : Arrays.asList(child1Id, child2Id, otherParentId, parentId)) {
      if (orgId == null) {
        continue;
      }
      logger.info("Deleting org with id {}...", orgId);
      Organization organization = null;
      try {
        organization = manager.read(orgId);
      } catch (Exception e) {
        fail("Could not read Organization to delete " + (organization != null ? organization : ("with id " + orgId)),
            e);
      }

      manager.delete(organization);
    }
    logger.warn("Test '{}' end", testInfo.getDisplayName());
  }

  @Test
  @DisplayName("Updating other parent organizations with child from old parent should implicitly update old parent")
  void moveChildrenOrganization() throws Exception {
    Organization otherParent = manager.read(otherParentId);
    Organization child1 = manager.read(child1Id);

    logger.info("Updating organizations of {}", otherParent);

    otherParent.setOrganizations(Arrays.asList(child1));
    otherParent = manager.update(otherParent);

    logger.info("Updated organizations of {}", otherParent);

    Organization parent = manager.read(parentId);

    Collection<Organization> childrenOrgs = parent.getOrganizations();

    logger.info("Checking children of parent {}", parent);

    assertThat(childrenOrgs).containsExactly(manager.read(child2Id));
  }

  @Test
  @DisplayName("Updating parent from child of another parent should implicitly update parent")
  void setNewParentOrganization() throws Exception {
    Organization otherParent = manager.read(otherParentId);
    Organization child1 = manager.read(child1Id);

    logger.info("Updating parent of {}", child1);

    child1.setParent(otherParent);
    child1 = manager.update(child1);

    logger.info("Updated parent of {}", child1);

    Organization parent = manager.read(parentId);

    Collection<Organization> childrenOrgs = parent.getOrganizations();

    logger.info("Checking children of parent {}", parent);

    assertThat(childrenOrgs).containsExactly(manager.read(child2Id));
  }

  @Test
  @DisplayName("Creating children with parent organization should update the parent order")
  void creatingOtherChildrenWithParentOrg() throws Exception {
    Organization otherParent = manager.read(otherParentId);

    Organization someChild = newOrg("Some Child");
    someChild.setParent(otherParent);

    someChild = manager.create(someChild);
    Organization someOtherChild = newOrg("Some Other Child");
    someOtherChild.setParent(otherParent);
    someOtherChild = manager.create(someOtherChild);

    assertThat(otherParent.getOrganizations()).containsExactly(someChild, someOtherChild);
    logger.info("Trying update");
    manager.update(otherParent);

    otherParent = manager.read(otherParentId);

    assertThat(otherParent.getOrganizations()).containsExactly(someChild, someOtherChild);
  }

  private static Organization newOrg(final String name, final Organization... organizations) {
    Organization org = new Organization();
    // adding TEST_NUMBER to handle test failing to delete some entities, causing
    // the following tests to fail in the prepareEntities
    org.setName(name + " - (" + TEST_NUMBER + ")");
    org.setDisplayName(name);
    org.setOrganizations(Arrays.asList(organizations));
    return org;
  }

}
