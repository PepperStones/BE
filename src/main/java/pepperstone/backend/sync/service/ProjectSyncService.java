package pepperstone.backend.sync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pepperstone.backend.common.entity.ProjectsEntity;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.ProjectsRepository;
import pepperstone.backend.common.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectSyncService {
    private final SyncService syncService;
    private final UserRepository userRepository;
    private final ProjectsRepository projectsRepository;

    private static final String RANGE = "'참고. 전사 프로젝트'!A1:Z100";

    @Transactional
    public void sync(String spreadsheetId) {
        List<List<Object>> data = syncService.readSheet(spreadsheetId, RANGE);

        if (data == null || data.size() <= 7) {
            throw new RuntimeException("Insufficient data in the spreadsheet.");
        }

        // 전사 프로젝트 정보 가져오기(사번, 이름, 전사 프로젝트명, 부여경험치)
        List<Map<String, Object>> projectList = projectList(data);

        // 전사 프로젝트 동기화
        for (Map<String, Object> projectInfo : projectList) {
            String companyNum = projectInfo.get("companyNum").toString();
            String name = projectInfo.get("name").toString();
            String projectName = projectInfo.get("projectName").toString();
            int experience = (int) projectInfo.get("experience");

            // users 테이블에서 해당 사원 조회
            UserEntity user = userRepository.findByCompanyNumAndName(companyNum, name)
                    .orElseThrow(() -> new RuntimeException("User not found: " + companyNum + ", " + name));

            // 이미 동일한 프로젝트가 저장되어 있는지 확인
            boolean projectExists = projectsRepository.existsByUsersAndProjectName(user, projectName);
            if (projectExists) {
                System.out.println("이미 등록된 프로젝트입니다: " + projectName + " (" + companyNum + ")");
                continue;
            }

            // 새로운 프로젝트 생성 및 저장
            ProjectsEntity project = new ProjectsEntity();
            project.setUsers(user);
            project.setProjectName(projectName);
            project.setExperience(experience);
            project.setCreatedAt(LocalDate.now());

            projectsRepository.save(project);
        }

    }

    private int parseInteger(List<Object> row, int index) {
        try {
            return row.size() > index && !row.get(index).toString().isEmpty() ? Integer.parseInt(row.get(index).toString().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 전사 프로젝트 정보를 저장한 리스트 반환 메서드
    private List<Map<String, Object>> projectList(List<List<Object>> data){
        // 전사 프로젝트 정보를 저장할 리스트
        List<Map<String, Object>> projectList = new ArrayList<>();

        // 8번째 행에서 정보 읽기
        int startRow = 7;
        // 사번이 빈칸이면 종료
        while(startRow < data.size() && data.get(startRow).size() > 6 && !data.get(startRow).get(3).toString().isEmpty()) {
            List<Object> projectRow = data.get(startRow);

            // 프로젝트 사원 및 경험치 정보
            String companyNum = projectRow.get(3).toString().trim(); // 사번
            String name = projectRow.get(4).toString().trim(); // 이름
            String projectName = projectRow.get(5).toString().trim(); // 전사 프로젝트명
            int experience = parseInteger(projectRow, 6); // 부여 경험치

            // 사원 정보를 맵으로 저장
            Map<String, Object> peopleInfo = new HashMap<>();
            peopleInfo.put("companyNum", companyNum);
            peopleInfo.put("name", name);
            peopleInfo.put("projectName", projectName);
            peopleInfo.put("experience", experience);

            // 리스트에 사원 정보 추가
            projectList.add(peopleInfo);

            startRow++;
        }

        return projectList;
    }
}
