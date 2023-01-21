package org.jboss.quickstarts.wfk.contact;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
/*@NamedQueries({
        @NamedQuery(name = Review.FIND_ALL, query = "SELECT c FROM Review c ORDER BY c.lastName ASC, c.firstName ASC"),
        @NamedQuery(name = Review.FIND_BY_EMAIL, query = "SELECT c FROM Review c WHERE c.email = :email")
})*/
@XmlRootElement
@Table(name = "review")
public class Review implements Serializable {
    /** Default value included to remove warning. Remove or modify at will. **/
    private static final long serialVersionUID = 1L;

    //public static final String FIND_ALL = "Review.findAll";
    //public static final String FIND_BY_EMAIL = "Review.findByEmail";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @NotNull
    @Size(min = 1, max = 300)
    @Pattern(regexp = "[A-Za-z-']+", message = "Please use a name without numbers or specials")
    @Column(name = "review")
    private String review;
    
    @NotNull
    @Min(0)
    @Max(5)
    @Pattern(regexp = "\\d", message = "Please use a number between 0 and 5")
    @Column(name = "rating")
    private String rating;

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
    
}
