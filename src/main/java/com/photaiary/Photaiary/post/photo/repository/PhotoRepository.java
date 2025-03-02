package com.photaiary.Photaiary.post.photo.repository;

import com.photaiary.Photaiary.post.daily.entity.Daily;
import com.photaiary.Photaiary.post.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface PhotoRepository extends JpaRepository<Photo, Long> , PhotoCustomRepository{
    Photo save(Photo photo);

    Optional<Photo> findById(Long id);

    void deleteById(Long id);

    List<Photo> findAllByDaily(Daily daily);


//    List<Photo> findByDailyIndex(Long dailyIndex);
}
