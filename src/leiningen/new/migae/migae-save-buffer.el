;; to install:
;; assuming you byte compile it in ~/.emacs.d/elisp/,
;; add this to your init file:
;; (load (expand-file-name "~/.emacs.d/elisp/migae-save-buffer"))
;; (add-hook 'clojure-mode-hook
;;              (lambda ()
;;                (define-key clojure-mode-map "\C-xC-s"
;;                            'migae-save-buffer)))

(defun migae-save-buffer (&optional args)
  (interactive "p")
  (save-buffer)
  (if (assoc 'srcroot file-local-variables-alist)
      (let* ((srcroot (file-name-as-directory (substitute-in-file-name (cdr (assoc 'srcroot file-local-variables-alist)))))
	     (warroot (file-name-as-directory (substitute-in-file-name (cdr (assoc 'warroot file-local-variables-alist)))))
	     (srcfile (buffer-file-name))
	     (relname (file-relative-name srcfile srcroot))
	     (tgtfile (concat warroot "WEB-INF/classes/" relname)))
	(message "copying %s to %s..." srcfile tgtfile)
	(make-directory (file-name-directory tgtfile) t)
	(copy-file srcfile tgtfile t))))

(provide 'migae-save-buffer)
