package it.uniroma3.siw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.CuocoRepository;

/**
 * The CuocoService handles logic for Cuocos.
 */
@Service
public class CuocoService {

    @Autowired
    protected CuocoRepository CuocoRepository;
    

    @Autowired
    protected CredentialsRepository credentialsRepository;
    
    public Optional<Cuoco> getUserByCredentials(UserDetails userDetails) {
        String username = userDetails.getUsername();
            return credentialsRepository.findByUsername(username).map(Credentials::getUser);
        }
  

    /**
     * This method retrieves a Cuoco from the DB based on its ID.
     * @param id the id of the Cuoco to retrieve from the DB
     * @return the retrieved Cuoco, or null if no Cuoco with the passed ID could be found in the DB
     */
    @Transactional
    public Cuoco getCuoco(Long id) {
        Optional<Cuoco> result = this.CuocoRepository.findById(id);
        return result.orElse(null);
    }

    /**
     * This method saves a Cuoco in the DB.
     * @param Cuoco the Cuoco to save into the DB
     * @return the saved Cuoco
     * @throws DataIntegrityViolationException if a Cuoco with the same Cuoconame
     *                              as the passed Cuoco already exists in the DB
     */
    @Transactional
    public Cuoco saveCuoco(Cuoco Cuoco) {
        return this.CuocoRepository.save(Cuoco);
    }

    /**
     * This method retrieves all Cuocos from the DB.
     * @return a List with all the retrieved Cuocos
     */
    @Transactional
    public List<Cuoco> getAllCuocos() {
        List<Cuoco> result = new ArrayList<>();
        Iterable<Cuoco> iterable = this.CuocoRepository.findAll();
        for(Cuoco Cuoco : iterable)
            result.add(Cuoco);
        return result;
    }
    

}