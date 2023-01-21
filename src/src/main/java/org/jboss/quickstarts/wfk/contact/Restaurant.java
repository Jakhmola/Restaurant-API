package org.jboss.quickstarts.wfk.contact;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = Restaurant.FIND_ALL, query = "SELECT c FROM Restaurant c ORDER BY c.name ASC"),
        @NamedQuery(name = Restaurant.FIND_BY_PHONENO, query = "SELECT c FROM Restaurant c WHERE c.phoneNumber = :phoneNumber")
})
@XmlRootElement
@Table(name = "restaurant", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
public class Restaurant implements Serializable {
    /** Default value included to remove warning. Remove or modify at will. **/
    private static final long serialVersionUID = 1L;

    public static final String FIND_ALL = "Restaraunt.findAll";
    public static final String FIND_BY_PHONENO = "Restaraunt.findByEmail";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z- ']+", message = "Please use a name without numbers or specials")
    @Column(name = "name")
    private String name;
    

    @NotNull
    @NotEmpty
    @Pattern(regexp = " [A-Za-z0-9]{6}")
    private String postCode;

    @NotNull
    @Pattern(regexp = "0[0-9]{10}")
    @Column(name = "phone_number")
    private String phoneNumber;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPost_code() {
		return postCode;
	}

	public void setPost_code(String post_code) {
		this.postCode = post_code;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(phoneNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Restaurant other = (Restaurant) obj;
		return Objects.equals(phoneNumber, other.phoneNumber);
	}
}
