package org.jboss.quickstarts.wfk.contact;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = Review.FIND_ALL, query = "SELECT c FROM Review c ORDER BY c.userId ASC, c.restaurantId ASC"),
        @NamedQuery(name = Review.FIND_BY_USER_ID, query = "SELECT c FROM Review c WHERE c.userId = :userId")
})
@XmlRootElement
@Table(name = "review")

public class Review implements Serializable {
    /** Default value included to remove warning. Remove or modify at will. **/
    private static final long serialVersionUID = 1L;

    public static final String FIND_ALL = "Review.findAll";
    public static final String FIND_BY_USER_ID = "Review.findByUserId";
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    
    @Column(name = "user")
    private Long userId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    
    @NotNull
    @Column(name = "restaurant")
    private Long restaurantId;
    
    @NotNull
    @Size(min = 1, max = 300)
    @Pattern(regexp = "[A-Za-z-' ,.]+", message = "Please use a text without numbers or specials")
    @Column(name = "review")
    private String review;
    
    @NotNull
    @Pattern(regexp = "[0-5]", message = "Please use a number between 0 and 5")
    @Column(name = "rating")
    private String rating;

	public User getUser() {
		return user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
	}

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
	
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		return Objects.hash(restaurantId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Review other = (Review) obj;
		return Objects.equals(restaurantId, other.restaurantId) && Objects.equals(userId, other.userId);
	}

	@Override
	public String toString() {
		return "Review [id=" + id + ", userId=" + userId + ", user=" + user+", restaurantId=" + restaurantId
				+ ", review=" + review + ", rating=" + rating + "]";
	}

    
}
