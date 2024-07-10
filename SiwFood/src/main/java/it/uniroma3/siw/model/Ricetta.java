 package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Ricetta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

	private String name;
	
    @OneToMany
    private List<Image> images ;
	
	@OneToMany(mappedBy= "ricetta", cascade=CascadeType.ALL)
	private List<RicettaIngrediente> ingredienti= new ArrayList<>();
	
	private String descrizione;
	
	@ManyToOne
	private Cuoco cuoco; 
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	




	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ricetta other = (Ricetta) obj;
		return Objects.equals(id, other.id) && Objects.equals(id, other.id);
	}



	public List<RicettaIngrediente> getIngredienti() {
		return ingredienti;
	}

	public void setIngredienti(List<RicettaIngrediente> ingredienti) {
		this.ingredienti = ingredienti;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public Cuoco getCuoco() {
		return cuoco;
	}
	
	public void setCuoco(Cuoco cuoco) {
		this.cuoco = cuoco;
	}
}
