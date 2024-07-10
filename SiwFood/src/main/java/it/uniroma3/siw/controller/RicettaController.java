package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Ricetta;
import it.uniroma3.siw.model.RicettaIngrediente;
import it.uniroma3.siw.repository.CuocoRepository;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.repository.IngredienteRepository;
import it.uniroma3.siw.repository.RicettaRepository;
import it.uniroma3.siw.repository.RicettaIngredienteRepository;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.CuocoService;

@Controller
public class RicettaController {

	
	@Autowired 
	private RicettaRepository ricettaRepository;
	
	@Autowired 
	private CuocoRepository cuocoRepository;
	
	@Autowired 
	private CuocoService cuocoService;
	
	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private IngredienteRepository ingredienteRepository;
	
	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private RicettaIngredienteRepository RicettaIngredienteRepository;
	
	@GetMapping("/ricette")
	public String getRicette(Model model) {		
		model.addAttribute("ricette", this.ricettaRepository.findAll());
		return "ricette.html";
	}
	
	@GetMapping("/ricetteCuoco")
	public String getRicetteCuoco(Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Cuoco currentUser = cuocoService.getUserByCredentials(userDetails).orElseThrow(() -> new RuntimeException("User not found"));
		List<Ricetta> ricette= currentUser.getRicette();
		model.addAttribute("ricette", ricette);
		return "ricetteCuoco.html";
	}
	
	@GetMapping("/ricetta/{id}")
	public String getRicetta(@PathVariable("id") Long id, Model model) {
		Ricetta ricetta= this.ricettaRepository.findById(id).get();
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("cuoco", ricetta.getCuoco());
		return "ricetta.html";
	}
	
	@GetMapping("/addRicettaAdmin")
	public String formNewRicetta(Model model) {
		model.addAttribute("ricetta", new Ricetta());
		model.addAttribute("cuochi", this.cuocoRepository.findAll());
		return "admin/addRicetta.html";
	}
	
	@PostMapping("/adminAddRicetta")
    public String addRicetta(@ModelAttribute("ricetta") Ricetta ricetta, 
    		                    Model model,
    		                    @RequestParam("files") MultipartFile[] images) throws IOException {
        
	    List<Image> imageList = new ArrayList<>();
	    for (MultipartFile image : images) {
	        Image img = new Image(image.getBytes());
	        this.imageRepository.save(img);
	        imageList.add(img);
	    }
	    ricetta.setImages(imageList);

        this.ricettaRepository.save(ricetta);


	   return "redirect:/admin/indexRicette";  // Redirect o nome della vista dopo il salvataggio

    }
	
	@GetMapping("/modificaricetta/{id}")
	public String formAggiornaRicetta(@PathVariable("id") Long id, Model model) {		
		Ricetta ricetta = ricettaRepository.findById(id).get();
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredientiRicetta", ricetta.getIngredienti());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());
		model.addAttribute("cuochi", this.cuocoRepository.findAll());

		return "admin/formAggiornaRicetta.html";
	}

	@GetMapping(value="/admin/indexRicette")
	public String indexRicetta(Model model) {
		model.addAttribute("ricette", this.ricettaRepository.findAll());
		return "admin/indexRicette.html";
	}
	
	
	@GetMapping("/cancellaricetta/{ricettaId}")
	public String cancellaRicetta(Model model, @PathVariable("ricettaId") Long ricettaId ) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		this.ricettaRepository.delete(ricetta);
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			return "redirect:/admin/indexRicette";  // Redirect o nome della vista dopo il salvataggio
		}
		
	
		return "redirect:/ricetteCuoco";
	}
	
	
	@PostMapping("/cambioCuocoRicetta")
    public String cambioCuoco(@RequestParam("id") Long id, @RequestParam("cuoco") Cuoco cuoco) {
        
		Ricetta ricetta=this.ricettaRepository.findById(id).get();
    	ricetta.setCuoco(cuoco);
        this.ricettaRepository.save(ricetta);
	    return "redirect:/admin/indexRicette";  // Redirect o nome della vista dopo il salvataggio
    }
	
	
	
	

	
	@PostMapping("/admin/addIngrediente/{ricettaId}")
	public String addIngredienteToRicetta(@PathVariable("ricettaId") Long ricettaId, @RequestParam String name, @RequestParam String quantita, Model model) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		Ingrediente ingrediente = new Ingrediente();
		ingrediente.setName(name);
	//	ingrediente.setQuantita(quantita);
	// 	ricetta.getIngredienti().add(ingrediente);
		this.ingredienteRepository.save(ingrediente);
		this.ricettaRepository.save(ricetta);
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredienti", ricetta.getIngredienti());
		return "admin/formAggiornaRicetta.html";
	}

	@GetMapping("/admin/removeIngrediente/{ricettaId}/{ingredienteId}")
	public String removeIngredienteFromRicetta(@PathVariable("ricettaId") Long ricettaId, @PathVariable("ingredienteId") Long ingredienteId, Model model) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		Ingrediente ingrediente = this.ingredienteRepository.findById(ingredienteId).get();
        ricetta.getIngredienti().remove(ingrediente);
		this.ricettaRepository.save(ricetta);
		model.addAttribute("ricetta", ricetta);
		return "redirect:/admin/formAggiornaRicetta";
	}


	@GetMapping(value="/admin/indexAdmin")
	public String indexAdmin(Model model) {
		return "admin/indexAdmin.html";
	}
	
	/* Cuoco */
	
	@GetMapping("/addRicettaCuoco")
	public String formNewRicetta1(Model model) {
		model.addAttribute("ricetta", new Ricetta());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());
		return "addRicetta.html";
	}
	
	@PostMapping("/cuocoAddRicetta")
    public String cuocoAddRicetta(@ModelAttribute("ricetta") Ricetta ricetta, 
    		                    Model model,
    		                    @RequestParam("files") MultipartFile[] images) throws IOException {
        
        List<Image> imageList = new ArrayList<>();
        for (MultipartFile image : images) {
            Image img = new Image(image.getBytes());
            this.imageRepository.save(img);
            imageList.add(img);
        }
        ricetta.setImages(imageList);
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.DEFAULT_ROLE)){
			Cuoco currentUser = cuocoService.getUserByCredentials(userDetails).orElseThrow(() -> new RuntimeException("User not found"));
			ricetta.setCuoco(currentUser);
		}
        this.ricettaRepository.save(ricetta);


	return "redirect:/ricette";  // Redirect o nome della vista dopo il salvataggio

    }


	
	@GetMapping("/addIngredienteCuoco")
	public String formNewIngrediente1(Model model) {
		model.addAttribute("ingrediente", new Ingrediente());
		return "addIngrediente.html";
	}
	
	@PostMapping("/cuocoAddIngrediente")
    public String cuocoAddIngrediente(@ModelAttribute("ingrediente") Ingrediente ingrediente, 
    		                    Model model) throws IOException {
        

        this.ingredienteRepository.save(ingrediente);
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			return "redirect:/admin/indexRicette";
		}
        
        
        return "redirect:/ricette";  // Redirect o nome della vista dopo il salvataggio

    }
	
	@GetMapping("/modificaRicettaCuoco/{id}")
	public String formModificaRicettaCuoco(@PathVariable("id") Long id, Model model) {
		Ricetta ricetta = ricettaRepository.findById(id).get();
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredientiRicetta", ricetta.getIngredienti());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());
		return "modificaRicettaCuoco";
	}
	
	
	@PostMapping("/cambioNomeRicetta")
    public String cambioNomeRicetta(@RequestParam("id") Long id, @RequestParam("name") String name) {
        
		Ricetta ricetta=this.ricettaRepository.findById(id).get();
        ricetta.setName(name);
        this.ricettaRepository.save(ricetta);
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			return "redirect:/admin/indexRicette";
		}
        
	    return "redirect:/ricetteCuoco";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/cambioDescRicetta")
    public String cambioDescRicetta(@RequestParam("id") Long id, @RequestParam("descrizione") String descrizione) {
        
		Ricetta ricetta=this.ricettaRepository.findById(id).get();
        ricetta.setDescrizione(descrizione);
        this.ricettaRepository.save(ricetta);
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			return "redirect:/admin/indexRicette";
		}
        
	    return "redirect:/ricetteCuoco";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/immagineUpdate")
    public String cambioImmRicetta(@RequestParam("id") Long id, @RequestParam("files") MultipartFile[] images)throws IOException {
        
		Ricetta ricetta=this.ricettaRepository.findById(id).get();
		
    	List<Image> imageList = new ArrayList<>();
    	for (MultipartFile image : images) {
    		Image img = new Image(image.getBytes());
    		this.imageRepository.save(img);
    		imageList.add(img);
    	}
    	 ricetta.getImages().addAll(imageList);

        this.ricettaRepository.save(ricetta);
        
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			return "redirect:/admin/indexRicette";
		}
		
	    return "redirect:/ricetteCuoco";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/addIngrediente/{ricettaId}")
    public String addIngredinete(@RequestParam("id") Long id, @RequestParam("ingrediente") Ingrediente ingrediente,
								@RequestParam("quantita") String quantita) {
        
		Ricetta ricetta=this.ricettaRepository.findById(id).get();
		RicettaIngrediente ricettaIngrediente= new RicettaIngrediente();
		ricettaIngrediente.setIngrediente(ingrediente);
		ricettaIngrediente.setQuantita(quantita);
		ricettaIngrediente.setRicetta(ricetta);
		this.RicettaIngredienteRepository.save(ricettaIngrediente);
    	//ricetta.getIngredienti().add(ricettaIngrediente);       
    	
    	
        this.ricettaRepository.save(ricetta);
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			return "redirect:/admin/indexRicette";
		}
		
	    return "redirect:/ricetteCuoco";  // Redirect o nome della vista dopo il salvataggio
    }
	
	
/*	@PostMapping("/modificaRicettaCuoco")
    public String modificaRicettaCuoco(@RequestParam("id") Long id, @RequestParam("name") String name, @RequestParam("descrizione") String descrizione,
    									@RequestParam("files") MultipartFile[] images,
    									@Param("ingrediente") Ingrediente ingrediente,
    									@Param("quantita") String quantita) throws IOException {
        
		Ricetta ricetta=this.ricettaRepository.findById(id).get();
		ricetta.setName(name);
        ricetta.setDescrizione(descrizione);
        
        if(images!=null) {
        	List<Image> imageList = new ArrayList<>();
        	for (MultipartFile image : images) {
        		Image img = new Image(image.getBytes());
        		this.imageRepository.save(img);
        		imageList.add(img);
        	}
        	ricetta.getImages().addAll(imageList);
        }
        
        if(ingrediente!=null && quantita !=null) {
        	ricetta.getIngredienti().add(ingrediente);       
        	ingrediente.setQuantita(quantita);
        }

        this.ricettaRepository.save(ricetta);
	    return "redirect:/ricetteCuoco";  // Redirect o nome della vista dopo il salvataggio
    } */
	
	@GetMapping("/removeImage/{ricettaId}/{imageId}")
	public String cancellaImmagine(Model model, @PathVariable("ricettaId") Long ricettaId, @PathVariable("imageId") Long imageId ) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		Image image = this.imageRepository.findById(imageId).get();
		ricetta.getImages().remove(image);
        this.ricettaRepository.save(ricetta);
        
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredientiRicetta", ricetta.getIngredienti());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());
		
		
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			model.addAttribute("cuochi", this.cuocoRepository.findAll());
			return "/admin/formAggiornaRicetta";
		}

		return "modificaRicettaCuoco";
	}
	
	@GetMapping("/removeIngrediente/{ricettaId}/{ingredienteId}")
	public String cancellaIngrediente(Model model, @PathVariable("ricettaId") Long ricettaId, @PathVariable("ingredienteId") Long ingredienteId ) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		RicettaIngrediente ricettaIngrediente = this.RicettaIngredienteRepository.findById(ingredienteId).get();
		ricetta.getIngredienti().remove(ricettaIngrediente);
		this.RicettaIngredienteRepository.delete(ricettaIngrediente);
        this.ricettaRepository.save(ricetta);
        
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredientiRicetta", ricetta.getIngredienti());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());
		
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			model.addAttribute("cuochi", this.cuocoRepository.findAll());
			return "/admin/formAggiornaRicetta";
		}

		return "modificaRicettaCuoco";
	}
	
	
	
	
	

	
}
