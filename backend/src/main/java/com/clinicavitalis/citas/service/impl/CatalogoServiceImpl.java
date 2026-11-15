package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.EspecialidadDTO;
import com.clinicavitalis.citas.dto.MedicoDTO;
import com.clinicavitalis.citas.repository.EspecialidadRepository;
import com.clinicavitalis.citas.repository.MedicoRepository;
import com.clinicavitalis.citas.service.CatalogoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {

    private final EspecialidadRepository especialidadRepository;
    private final MedicoRepository medicoRepository;

    public CatalogoServiceImpl(EspecialidadRepository especialidadRepository, MedicoRepository medicoRepository) {
        this.especialidadRepository = especialidadRepository;
        this.medicoRepository = medicoRepository;
    }

    @Override
    public List<EspecialidadDTO> listarEspecialidades() {
        return especialidadRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(EspecialidadDTO::de)
                .toList();
    }

    @Override
    public List<MedicoDTO> listarMedicos(Long idEspecialidad) {
        return medicoRepository.buscarActivos(idEspecialidad).stream()
                .map(MedicoDTO::de)
                .toList();
    }
}
