package no.fintlabs.membership;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class KafkaMembership {
    private Long roleId;
    private Long memberId;
    private String memberStatus;
    private Date memberStatusChanged;
}
