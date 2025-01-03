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
    private final List<String> userTypes;
    private final Boolean getAggregatedRoles;

    public RoleSpecificationBuilder(
            String search,
            List<String> filteredOrgUnits,
            List<String> orgUnitsInScope,
            List<String> userTypes,
            Boolean getAggregatedRoles
    ) {
        this.searchString = search;
        this.filteredOrgUnits = filteredOrgUnits;
        this.orgUnitsInScope = orgUnitsInScope;
        this.userTypes = userTypes;
        this.getAggregatedRoles = getAggregatedRoles;
    }

    public Specification<Role> build() {
        Specification<Role> roleSpecification;

        if (searchString != null) {
            roleSpecification = roleNameLike(searchString);
        } else {
            roleSpecification = Specification.where(null);
        }
        if(!orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            roleSpecification = roleSpecification.and(belongsToOrgUnit(orgUnitsInScope));
        }
        if (filteredOrgUnits != null) {
            roleSpecification = roleSpecification.and(belongsToOrgUnit(filteredOrgUnits));
        }
        if (userTypes != null ) {
            roleSpecification = roleSpecification.and(belongsToUserType(userTypes));
        }
        if (getAggregatedRoles!=null) {
            roleSpecification = roleSpecification.and(isAggregatedRole(getAggregatedRoles));
        }
        return roleSpecification;
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
    private Specification<Role> belongsToUserType(List<String> orgUnits) {
        return (root, query, criteriaBuilder)-> criteriaBuilder.in(root.get("userType")).value(orgUnits);
    }
    private Specification<Role> isAggregatedRole(Boolean aggregatedRole) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("aggregatedRole"), aggregatedRole);
    }
}
