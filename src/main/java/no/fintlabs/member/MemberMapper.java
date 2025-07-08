package no.fintlabs.member;

public class MemberMapper {
    public static Member fromKontrollUser(KontrollUser kontrollUser) {
        return Member.builder()
                .id(kontrollUser.getId())
                .userName(kontrollUser.getUserName())
                .identityProviderUserObjectId(kontrollUser.getIdentityProviderUserObjectId())
                .firstName(kontrollUser.getFirstName())
                .lastName(kontrollUser.getLastName())
                .userType(kontrollUser.getUserType())
                .organisationUnitId(kontrollUser.getMainOrganisationUnitId())
                .organisationUnitName(kontrollUser.getMainOrganisationUnitName())
                .build();
    }
}
