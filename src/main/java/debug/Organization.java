package debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "customizable_organization", uniqueConstraints = {
    @UniqueConstraint(name = "uk_org_name", columnNames = "name") })
public class Organization {

  private UUID id;
  private String name;
  private String displayName;
  private Organization parent;
  private List<Organization> organizations = new ArrayList<>(0);

  @Id
  @Column(name = "id", unique = true, nullable = false, updatable = false, insertable = true)
  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  @Column(name = "name", nullable = false, length = 255)
  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Column(name = "displayName", nullable = false, length = 255)
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  public Organization getParent() {
    return this.parent;
  }

  public void setParent(final Organization parent) {
    if (parent == null || !parent.equals(this.getParent())) {
      removeFromParent();
    }
    this.parent = parent;
  }

  @OneToMany(fetch = FetchType.LAZY, orphanRemoval = false)
  @JoinColumn(name = "parent_id")
  @OrderColumn(name = "parent_order")
  public List<Organization> getOrganizations() {
    return this.organizations;
  }

  public void setOrganizations(final List<Organization> organizations) {
    organizations.stream().filter(org -> !this.equals(org.getParent())).forEach(org -> org.setParent(this));
    this.organizations = organizations;
  }

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    addToParent();
  }

  @PreUpdate
  void preUpdate() {
    addToParent();
  }

  private void addToParent() {
    if (parent != null && !parent.getOrganizations().contains(this)) {
      parent.getOrganizations().add(this);
    }
  }

  @PreRemove
  void removeFromParent() {
    if (parent != null) {
      parent.getOrganizations().remove(this);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    Organization other = (Organization) obj;
    return Objects.equals(id, other.id);
  }

  @Override
  public String toString() {
    return "Organization {id=" + id + ", name=" + name + ", displayName=" + displayName + "}";
  }

}
