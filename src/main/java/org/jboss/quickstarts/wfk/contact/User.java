package org.jboss.quickstarts.wfk.contact;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({ @NamedQuery(name = User.FIND_ALL, query = "SELECT c FROM User c ORDER BY c.name ASC"),
		@NamedQuery(name = User.FIND_BY_EMAIL, query = "SELECT c FROM User c WHERE c.email = :email") })
@XmlRootElement
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "User.findAll";
	public static final String FIND_BY_EMAIL = "User.findByEmail";

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
	@Email(message = "The email address must be in the format of name@domain.com")
	private String email;

	@NotNull
	@Pattern(regexp = "0[0-9]{10}")
	@Column(name = "phone_number")
	private String phoneNumber;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Review> reviews = new ArrayList<>();

	public void addReview(Review review) {
		reviews.add(review);
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static String getFindAll() {
		return FIND_ALL;
	}

	public static String getFindByEmail() {
		return FIND_BY_EMAIL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof User))
			return false;
		User user = (User) o;
		if (!email.equals(user.email))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(email);
	}
}