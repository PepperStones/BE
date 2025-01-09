package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectSyncService {
    private final SyncService syncService;
}
