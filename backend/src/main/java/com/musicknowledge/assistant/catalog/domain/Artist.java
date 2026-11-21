package com.musicknowledge.assistant.catalog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artists")
public class Artist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(length = 2000)
  private String biography;

  @Column(length = 100)
  private String country;

  @Column(name = "active_from")
  private LocalDate activeFrom;

  @Column(name = "active_to")
  private LocalDate activeTo;

  @OneToMany(mappedBy = "artist")
  @JsonIgnore
  private List<Album> albums = new ArrayList<>();

  public Artist() {}

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

  public String getBiography() {
    return biography;
  }

  public void setBiography(String biography) {
    this.biography = biography;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public LocalDate getActiveFrom() {
    return activeFrom;
  }

  public void setActiveFrom(LocalDate activeFrom) {
    this.activeFrom = activeFrom;
  }

  public LocalDate getActiveTo() {
    return activeTo;
  }

  public void setActiveTo(LocalDate activeTo) {
    this.activeTo = activeTo;
  }

  public List<Album> getAlbums() {
    return albums;
  }

  public void setAlbums(List<Album> albums) {
    this.albums = albums;
  }
}
