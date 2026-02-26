package org.example.mopl.watchtogether.controller;

import lombok.RequiredArgsConstructor;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.dto.WatchingSessionDto;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WatchTogetherController {

    private final WatchTogetherService watchTogetherService;

    @GetMapping("/users/{watcherId}/watching-sessions")
    private ResponseEntity<WatchingSessionDto> getWatcher(
            @PathVariable String watcherId
    ){
        WatchingSessionDto res = watchTogetherService.getWatcher(watcherId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/contents/{contentId}/watching-sessions")
    private ResponseEntity<CursorResponseWatchingSessionDto> getWatcherList(
            @PathVariable String contentId,
            @RequestParam(required = false) String watcherNameLike,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String idAfter,
            @RequestParam Integer limit,
            @RequestParam String sortDirection,
            @RequestParam String sortBy
    ){
        CursorResponseWatchingSessionDto res = watchTogetherService.getWatcherList(
                contentId,
                watcherNameLike,
                cursor,
                idAfter,
                limit,
                sortDirection,
                sortBy
        );
        return ResponseEntity.ok(res);
    }

}
