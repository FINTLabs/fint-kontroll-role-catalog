package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

import no.fintlabs.opa.model.OrgUnitType;

@Slf4j
public class RoleSpecificationBuilder {
    private final String searchString;
    private final List<String> filteredOrgUnits;
    private final List<String> orgUnitsInScope;
    private final List<String> roleTypes;
    private final Boolean getAggregatedRoles;

    public RoleSpecificationBuilder(
            String search,
            List<String> filteredOrgUnits,
            List<String> orgUnitsInScope,
            List<String> roleTypes,
            Boolean getAggregatedRoles
    ) {
        this.searchString = search;
        this.filteredOrgUnits = filteredOrgUnits;
        this.orgUnitsInScope = orgUnitsInScope;
        this.roleTypes = roleTypes;
        this.getAggregatedRoles = getAggregatedRoles;
    }

    public Specification<Role> build() {
        Specification<Role> roleSpecification = Specification.where(roleIsActive());

        if (searchString != null) {
            roleSpecification = roleSpecification.and(roleNameLike(searchString));
        }
        if(!orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            roleSpecification = roleSpecification.and(belongsToOrgUnit(orgUnitsInScope));
        }
        if (filteredOrgUnits != null) {
            roleSpecification = roleSpecification.and(belongsToOrgUnit(filteredOrgUnits));
        }
        if (roleTypes != null ) {
            roleSpecification = roleSpecification.and(belongsToRoleType(roleTypes));
        }
        if (getAggregatedRoles!=null) {
            roleSpecification = roleSpecification.and(isAggregatedRole(getAggregatedRoles));
        }
        return roleSpecification;
    }

private Specification<Role> roleIsActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(criteriaBuilder.equal(criteriaBuilder.lower(root.get("roleStatus")), "active"),
                        criteriaBuilder.isNull(root.get("roleStatus")));
    }

    private Specification<Role> roleNameLike(String search) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("roleName")), "%" + search.toLowerCase() + "%");
    }
    private  Specification<Role> roleTypeEquals(String roleType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("roleType")), roleType);
    }
    private Specification<Role> belongsToOrgUnit(List<String> orgUnits) {
        return (root, query, criteriaBuilder)-> criteriaBuilder.in(root.get("organisationUnitId")).value(orgUnits);
    }
    private Specification<Role> belongsToRoleType(List<String> roleTypes) {
        return (root, query, criteriaBuilder)-> criteriaBuilder.in(root.get("roleType")).value(roleTypes);
    }
    private Specification<Role> isAggregatedRole(Boolean aggregatedRole) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("aggregatedRole"), aggregatedRole);
    }
}
