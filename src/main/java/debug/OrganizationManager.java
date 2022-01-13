package debug;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class OrganizationManager {

  private final JpaRepository<Organization, UUID> jpa;

  public OrganizationManager(final EntityManager entityManager) {
    JpaEntityInformation<Organization, ?> entityInformation = JpaEntityInformationSupport
        .getEntityInformation(Organization.class,
            entityManager);
    this.jpa = new SimpleJpaRepository<>(entityInformation, entityManager);
  }

  @Transactional
  public Organization read(final UUID id) {
    return jpa.findById(id).orElseThrow(() -> new NoSuchElementException("No organization with id " + id + " exists"));
  }

  @Transactional
  public Organization create(final Organization org) {
    return jpa.save(org);
  }

  @Transactional
  public Organization update(final Organization org) {
    return jpa.save(org);
  }

  @Transactional
  public void delete(final Organization org) {
    jpa.delete(org);
  }
}
