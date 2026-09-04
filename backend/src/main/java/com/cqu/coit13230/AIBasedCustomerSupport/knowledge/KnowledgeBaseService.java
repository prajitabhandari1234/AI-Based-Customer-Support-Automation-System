package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import com.cqu.coit13230.AIBasedCustomerSupport.common.NotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import com.cqu.coit13230.AIBasedCustomerSupport.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {
    private final KnowledgeBaseRepository entries;
    private final UserService users;

    public KnowledgeBaseService(KnowledgeBaseRepository entries, UserService users) {
        this.entries = entries;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseView> list() {
        return entries.findAllByOrderByCategoryAscQuestionPatternAsc().stream()
                .map(KnowledgeBaseView::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<KnowledgeMatch> findBestMatch(String message) {
        Set<String> words = words(message);
        if (words.isEmpty()) return Optional.empty();

        KnowledgeMatch best = null;
        for (KnowledgeBaseEntry entry : entries.findByActiveTrueOrderByCategoryAscQuestionPatternAsc()) {
            Set<String> patternWords = words(entry.getQuestionPattern());
            if (patternWords.isEmpty()) continue;
            long overlap = patternWords.stream().filter(words::contains).count();
            double score = overlap / (double) patternWords.size();
            String lower = message.toLowerCase(Locale.ROOT);
            for (String alternative : entry.getQuestionPattern().toLowerCase(Locale.ROOT).split("[,;|]")) {
                String trimmed = alternative.trim();
                if (trimmed.length() > 3 && lower.contains(trimmed)) score = Math.max(score, 0.95);
            }
            if (best == null || score > best.score()) best = new KnowledgeMatch(entry, score);
        }
        return best != null && best.score() >= 0.34 ? Optional.of(best) : Optional.empty();
    }

    @Transactional
    public KnowledgeBaseView create(KnowledgeBaseRequest request) {
        User current = users.currentUser();
        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();
        apply(entry, request, current);
        return KnowledgeBaseView.from(entries.save(entry));
    }

    @Transactional
    public KnowledgeBaseView update(Long id, KnowledgeBaseRequest request) {
        User current = users.currentUser();
        KnowledgeBaseEntry entry = entries.findById(id)
                .orElseThrow(() -> new NotFoundException("Knowledge base entry not found"));
        apply(entry, request, current);
        return KnowledgeBaseView.from(entries.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        if (!entries.existsById(id)) throw new NotFoundException("Knowledge base entry not found");
        entries.deleteById(id);
    }

    private void apply(KnowledgeBaseEntry entry, KnowledgeBaseRequest request, User user) {
        entry.setQuestionPattern(request.questionPattern().trim());
        entry.setAnswerTemplate(request.answerTemplate().trim());
        entry.setCategory(request.category());
        entry.setActive(request.active() == null || request.active());
        entry.setLastUpdatedBy(user);
    }

    private Set<String> words(String text) {
        if (text == null) return Set.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !Set.of("the", "and", "for", "with", "that", "this", "you", "your", "can", "how", "what").contains(word))
                .collect(Collectors.toSet());
    }
}
